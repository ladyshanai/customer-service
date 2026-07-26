package com.customer.service.exception;

import com.customer.service.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        var status = HttpStatus.NOT_FOUND;
        var body = buildErrorResponse(status, ex.getMessage(), request);
        log.info("Handled ResourceNotFoundException status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateCustomerException ex, HttpServletRequest request) {
        var status = HttpStatus.CONFLICT;
        var body = buildErrorResponse(status, ex.getMessage(), request);
        log.info("Handled DuplicateCustomerException status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex, HttpServletRequest request) {
        var status = HttpStatus.SERVICE_UNAVAILABLE;
        var body = buildErrorResponse(status, ex.getMessage(), request);
        log.warn("Handled ExternalServiceException status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseOperation(DatabaseOperationException ex, HttpServletRequest request) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        var body = buildErrorResponse(status, "An internal database error occurred.", request);
        log.error("Handled database operation exception status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler({InvalidCustomerIdException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        var body = buildErrorResponse(status, ex.getMessage(), request);
        log.warn("Handled bad request status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        var status = HttpStatus.BAD_REQUEST;
        var body = buildErrorResponse(status, message, request);
        log.warn("Handled validation error status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, HttpServletRequest request) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Handled unexpected exception status={} method={} path={} message={}",
                status.value(), request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        var body = buildErrorResponse(
                status,
                "An unexpected error occurred. Please contact support.",
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
    }
}
