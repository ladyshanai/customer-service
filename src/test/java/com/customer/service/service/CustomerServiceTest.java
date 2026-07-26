package com.customer.service.service;

import com.customer.service.client.ProductClient;
import com.customer.service.client.ProductClientDTO;
import com.customer.service.dto.CustomerDetailResponse;
import com.customer.service.dto.CustomerRequest;
import com.customer.service.dto.CustomerResponse;
import com.customer.service.entity.CustomerEntity;
import com.customer.service.exception.DatabaseOperationException;
import com.customer.service.exception.DuplicateCustomerException;
import com.customer.service.exception.ExternalServiceException;
import com.customer.service.exception.InvalidCustomerIdException;
import com.customer.service.exception.ResourceNotFoundException;
import com.customer.service.mapper.CustomerMapper;
import com.customer.service.repository.CustomerRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomerShouldReturnCreatedCustomer() {
        var request = buildCustomerRequest();
        var entity = buildCustomerEntity(1L);
        var response = buildCustomerResponse(1L);

        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerRepository.save(entity)).thenReturn(entity);
        when(customerMapper.toResponse(entity)).thenReturn(response);

        var result = customerService.createCustomer(request);

        assertEquals(1L, result.id());
        assertEquals("Ana", result.firstName());
        verify(customerMapper).toEntity(request);
        verify(customerRepository).save(entity);
        verify(customerMapper).toResponse(entity);
    }

    @Test
    void createCustomerShouldThrowDuplicateCustomerExceptionWhenUniqueConstraintFails() {
        var request = buildCustomerRequest();
        var entity = buildCustomerEntity(null);

        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerRepository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));

        var exception = assertThrows(
                DuplicateCustomerException.class,
                () -> customerService.createCustomer(request)
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void getAllCustomersShouldReturnMappedList() {
        var first = buildCustomerEntity(1L);
        var second = buildCustomerEntity(2L);
        var firstResponse = buildCustomerResponse(1L);
        var secondResponse = buildCustomerResponse(2L);

        when(customerRepository.findAll()).thenReturn(List.of(first, second));
        when(customerMapper.toResponse(first)).thenReturn(firstResponse);
        when(customerMapper.toResponse(second)).thenReturn(secondResponse);

        var result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    void getCustomerByIdShouldReturnCustomerWithProducts() {
        var customerId = 1L;
        var entity = buildCustomerEntity(customerId);
        var products = List.of(
                new ProductClientDTO(
                        10L, customerId, "ACCOUNT", "ACC-100",
                        BigDecimal.valueOf(2000), true, LocalDateTime.now(), LocalDateTime.now()
                )
        );
        var detailResponse = new CustomerDetailResponse(
                customerId,
                "Ana",
                "Lopez",
                "12345678",
                "Calle 123",
                "1111-1111",
                "ana@mail.com",
                "PERSON",
                true,
                BigDecimal.valueOf(500),
                products
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));
        when(productClient.getProductsByCustomer(customerId)).thenReturn(products);
        when(customerMapper.toDetailResponse(entity, products)).thenReturn(detailResponse);

        var result = customerService.getCustomerById(customerId);

        assertEquals(customerId, result.id());
        assertNotNull(result.products());
        assertEquals(1, result.products().size());
        verify(productClient).getProductsByCustomer(customerId);
        verify(customerMapper).toDetailResponse(entity, products);
    }

    @Test
    void getCustomerByIdShouldReturnEmptyProductsWhenProductServiceReturnsNull() {
        var customerId = 1L;
        var entity = buildCustomerEntity(customerId);
        var detailResponse = new CustomerDetailResponse(
                customerId,
                "Ana",
                "Lopez",
                "12345678",
                "Calle 123",
                "1111-1111",
                "ana@mail.com",
                "PERSON",
                true,
                BigDecimal.valueOf(500),
                List.of()
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));
        when(productClient.getProductsByCustomer(customerId)).thenReturn(null);
        when(customerMapper.toDetailResponse(entity, List.of())).thenReturn(detailResponse);

        var result = customerService.getCustomerById(customerId);

        assertNotNull(result.products());
        assertTrue(result.products().isEmpty());
        verify(customerMapper).toDetailResponse(entity, List.of());
    }

    @Test
    void getCustomerByIdShouldThrowInvalidCustomerIdExceptionWhenIdIsNotPositive() {
        assertThrows(
                InvalidCustomerIdException.class,
                () -> customerService.getCustomerById(0L)
        );
    }

    @Test
    void getCustomerByIdShouldThrowResourceNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomerById(99L)
        );
    }

    @Test
    void getCustomerByIdShouldWrapFeignExceptionAsExternalServiceException() {
        var customerId = 2L;
        var entity = buildCustomerEntity(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));
        when(productClient.getProductsByCustomer(customerId)).thenThrow(mock(FeignException.class));

        var exception = assertThrows(
                ExternalServiceException.class,
                () -> customerService.getCustomerById(customerId)
        );

        assertTrue(exception.getMessage().contains("Unable to communicate with product service"));
    }

    @Test
    void deleteCustomerShouldDeleteExistingCustomer() {
        var customerId = 5L;
        var entity = buildCustomerEntity(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));

        customerService.deleteCustomer(customerId);

        verify(customerRepository).delete(entity);
    }

    @Test
    void getAllCustomersShouldWrapDataAccessExceptionAsDatabaseOperationException() {
        when(customerRepository.findAll()).thenThrow(new DataAccessResourceFailureException("db down"));

        var exception = assertThrows(
                DatabaseOperationException.class,
                () -> customerService.getAllCustomers()
        );

        assertTrue(exception.getMessage().contains("Database error while fetching customers"));
    }

    @Test
    void updateCustomerShouldUpdateAndReturnCustomer() {
        var customerId = 7L;
        var request = buildCustomerRequest();
        var entity = buildCustomerEntity(customerId);
        var response = buildCustomerResponse(customerId);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));
        when(customerRepository.save(entity)).thenReturn(entity);
        when(customerMapper.toResponse(entity)).thenReturn(response);

        var result = customerService.updateCustomer(customerId, request);

        assertEquals(customerId, result.id());
        verify(customerMapper).updateEntityFromRequest(request, entity);
        verify(customerRepository).save(entity);
    }

    @Test
    void deleteCustomerShouldWrapDataAccessExceptionAsDatabaseOperationException() {
        var customerId = 8L;
        var entity = buildCustomerEntity(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(entity));
        doThrow(new DataAccessResourceFailureException("db down")).when(customerRepository).delete(any(CustomerEntity.class));

        var exception = assertThrows(
                DatabaseOperationException.class,
                () -> customerService.deleteCustomer(customerId)
        );

        assertTrue(exception.getMessage().contains("Database error while deleting customer"));
    }

    private CustomerRequest buildCustomerRequest() {
        return new CustomerRequest(
                "Ana",
                "Lopez",
                "12345678",
                "Calle 123",
                "1111-1111",
                "ana@mail.com",
                "PERSON",
                BigDecimal.valueOf(500)
        );
    }

    private CustomerEntity buildCustomerEntity(Long id) {
        var entity = new CustomerEntity();
        entity.setId(id);
        entity.setFirstName("Ana");
        entity.setLastNameOrCompanyName("Lopez");
        entity.setDocumentNumber("12345678");
        entity.setAddress("Calle 123");
        entity.setPhoneNumber("1111-1111");
        entity.setEmail("ana@mail.com");
        entity.setCustomerType("PERSON");
        entity.setActive(true);
        entity.setOutstandingBalance(BigDecimal.valueOf(500));
        return entity;
    }

    private CustomerResponse buildCustomerResponse(Long id) {
        return new CustomerResponse(
                id,
                "Ana",
                "Lopez",
                "12345678",
                "Calle 123",
                "1111-1111",
                "ana@mail.com",
                "PERSON",
                true,
                BigDecimal.valueOf(500)
        );
    }
}
