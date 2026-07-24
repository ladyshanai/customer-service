package com.customer.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CustomerRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        String lastNameOrCompanyName,
        @NotBlank(message = "Document number is required")
        String documentNumber,
        String address,
        String phoneNumber,
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,
        String customerType,
        BigDecimal outstandingBalance
) {
}
