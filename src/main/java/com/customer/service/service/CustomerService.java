package com.customer.service.service;

import com.customer.service.client.ProductClient;
import com.customer.service.client.ProductClientDTO;
import com.customer.service.dto.CustomerDetailResponse;
import com.customer.service.dto.CustomerRequest;
import com.customer.service.dto.CustomerResponse;
import com.customer.service.entity.CustomerEntity;
import com.customer.service.exception.DuplicateCustomerException;
import com.customer.service.exception.ExternalServiceException;
import com.customer.service.exception.InvalidCustomerIdException;
import com.customer.service.exception.ResourceNotFoundException;
import com.customer.service.mapper.CustomerMapper;
import com.customer.service.repository.CustomerRepository;
import feign.FeignException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class CustomerService {
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
        return executeDatabaseOperation(() -> {
            var customer = customerMapper.toEntity(request);
            return customerMapper.toResponse(saveCustomer(customer));
        }, "Database error while creating customer");
    }

    public List<CustomerResponse> getAllCustomers() {
        return executeDatabaseOperation(() ->
            customerRepository.findAll()
                    .stream()
                    .map(customerMapper::toResponse)
                    .toList(),
            "Database error while fetching customers"
        );
    }

    public CustomerDetailResponse getCustomerById(Long id) {
        var customer = findCustomer(id);
        return toDetailResponse(customer);
    }

    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request
    ) {
        return executeDatabaseOperation(() -> {
            var customer = findCustomer(id);

            customerMapper.updateEntityFromRequest(request, customer);

            return customerMapper.toResponse(saveCustomer(customer));
        }, "Database error while updating customer");
    }

    public void deleteCustomer(Long id) {
        executeDatabaseOperation(() -> {
            var customer = findCustomer(id);
            customerRepository.delete(customer);
            return null;
        }, "Database error while deleting customer");
    }

    private CustomerEntity findCustomer(Long id) {
        Objects.requireNonNull(id, "Customer id cannot be null");
        if (id <= 0) {
            throw new InvalidCustomerIdException("Customer id must be greater than zero");
        }

        return executeDatabaseOperation(() ->
            customerRepository.findById(id)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Customer not found with id: " + id
                            )
                    ),
            "Database error while fetching customer by id"
        );
    }

    private CustomerDetailResponse toDetailResponse(CustomerEntity customer) {
        return customerMapper.toDetailResponse(
                customer,
                findProductsByCustomer(customer.getId())
        );
    }

    private List<ProductClientDTO> findProductsByCustomer(Long customerId) {
        try {
            return productClient.getProductsByCustomer(customerId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Products not found for customer id: " + customerId
            );
        } catch (FeignException.FeignClientException e) {
            throw new ExternalServiceException(
                    "Invalid response while fetching customer products for customer id: " + customerId,
                    e
            );
        } catch (FeignException.FeignServerException e) {
            throw new ExternalServiceException(
                    "Product service unavailable while fetching products for customer id: " + customerId,
                    e
            );
        } catch (FeignException e) {
            throw new ExternalServiceException(
                    "Error while fetching customer products for customer id: " + customerId,
                    e
            );
        }
    }

    private CustomerEntity saveCustomer(CustomerEntity customer) {
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
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
            throw new ExternalServiceException(errorMessage, e);
        }
    }

}
