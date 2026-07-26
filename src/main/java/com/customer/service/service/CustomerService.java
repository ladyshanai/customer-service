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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class CustomerService {
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final ProductClient productClient;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            ProductClient productClient
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.productClient = productClient;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating customer");
        return executeDatabaseOperation(() -> {
            var customer = customerMapper.toEntity(request);
            var savedCustomer = saveCustomer(customer);
            log.info("Customer created successfully: id={}", savedCustomer.getId());
            return customerMapper.toResponse(savedCustomer);
        }, "Database error while creating customer");
    }

    public List<CustomerResponse> getAllCustomers() {
        log.debug("Fetching all customers");
        return executeDatabaseOperation(() ->
            customerRepository.findAll()
                    .stream()
                    .map(customerMapper::toResponse)
                    .toList(),
            "Database error while fetching customers"
        );
    }

    public CustomerDetailResponse getCustomerById(Long id) {
        log.debug("Fetching customer by id={}", id);
        var customer = findCustomer(id);
        return toDetailResponse(customer);
    }

    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request
    ) {
        log.info("Updating customer: id={}", id);
        return executeDatabaseOperation(() -> {
            var customer = findCustomer(id);

            customerMapper.updateEntityFromRequest(request, customer);

            var savedCustomer = saveCustomer(customer);
            log.info("Customer updated successfully: id={}", savedCustomer.getId());
            return customerMapper.toResponse(savedCustomer);
        }, "Database error while updating customer");
    }

    public void deleteCustomer(Long id) {
        log.info("Deleting customer: id={}", id);
        executeDatabaseOperation(() -> {
            var customer = findCustomer(id);
            customerRepository.delete(customer);
            log.info("Customer deleted successfully: id={}", id);
            return null;
        }, "Database error while deleting customer");
    }

    private CustomerEntity findCustomer(Long id) {
        if (id == null) {
            log.warn("Customer lookup rejected: id is null");
            throw new InvalidCustomerIdException("Customer id cannot be null");
        }
        if (id <= 0) {
            log.warn("Customer lookup rejected: invalid id={}", id);
            throw new InvalidCustomerIdException("Customer id must be greater than zero");
        }

        return executeDatabaseOperation(() -> {
                var customer = customerRepository.findById(id);
                if (customer.isEmpty()) {
                    log.warn("Customer not found: id={}", id);
                    throw new ResourceNotFoundException(
                            "Customer not found with id: " + id
                    );
                }
                return customer.get();
            },
            "Database error while fetching customer by id");
    }

    private CustomerDetailResponse toDetailResponse(CustomerEntity customer) {
        return customerMapper.toDetailResponse(
                customer,
                findProductsByCustomer(customer.getId())
        );
    }

    private List<ProductClientDTO> findProductsByCustomer(Long customerId) {
        try {
            log.debug("Calling product-service: customerId={}", customerId);
            var products = productClient.getProductsByCustomer(customerId);

            if (products == null) {
                log.warn("Product-service returned null product list: customerId={}", customerId);
                return List.of();
            }

            log.debug("Product-service call succeeded: customerId={}, productCount={}", customerId, products.size());
            return products;
        } catch (FeignException.NotFound e) {
            log.warn("Resource not found in product-service: customerId={}, status={}", customerId, e.status());
            throw new ResourceNotFoundException(
                    "Products not found for customer id: " + customerId
            );
        } catch (FeignException.FeignClientException e) {
            log.warn("Client error from product-service: customerId={}, status={}", customerId, e.status());
            throw new ExternalServiceException(
                    "Product service rejected the request for customer id: " + customerId,
                    e
            );
        } catch (FeignException.FeignServerException e) {
            log.error("Server error from product-service: customerId={}, status={}", customerId, e.status(), e);
            throw new ExternalServiceException(
                    "Product service is unavailable while fetching products",
                    e
            );
        } catch (FeignException e) {
            log.error("Error communicating with product-service: customerId={}, status={}", customerId, e.status(), e);
            throw new ExternalServiceException(
                    "Unable to communicate with product service",
                    e
            );
        }
    }

    private CustomerEntity saveCustomer(CustomerEntity customer) {
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate customer detected (document/email)");
            throw new DuplicateCustomerException(
                    "Customer with the same document number or email already exists",
                    e
            );
        }
    }

    private <T> T executeDatabaseOperation(Supplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            log.error(errorMessage, e);
            throw new DatabaseOperationException(errorMessage, e);
        }
    }

}
