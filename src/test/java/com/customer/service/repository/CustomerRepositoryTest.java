package com.customer.service.repository;

import com.customer.service.entity.CustomerEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveShouldPersistCustomer() {
        var customer = buildCustomer("12345678", "ana@mail.com");

        var saved = customerRepository.saveAndFlush(customer);

        assertNotNull(saved.getId());
        assertEquals("12345678", saved.getDocumentNumber());
        assertEquals("ana@mail.com", saved.getEmail());
    }

    @Test
    void saveShouldFailWhenDocumentNumberIsDuplicated() {
        customerRepository.saveAndFlush(buildCustomer("99999999", "first@mail.com"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(buildCustomer("99999999", "second@mail.com"))
        );
    }

    @Test
    void saveShouldFailWhenEmailIsDuplicated() {
        customerRepository.saveAndFlush(buildCustomer("11111111", "duplicate@mail.com"));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(buildCustomer("22222222", "duplicate@mail.com"))
        );
    }

    private CustomerEntity buildCustomer(String documentNumber, String email) {
        var customer = new CustomerEntity();
        customer.setFirstName("Ana");
        customer.setLastNameOrCompanyName("Lopez");
        customer.setDocumentNumber(documentNumber);
        customer.setAddress("Calle 123");
        customer.setPhoneNumber("1111-1111");
        customer.setEmail(email);
        customer.setCustomerType("PERSON");
        customer.setActive(true);
        customer.setOutstandingBalance(BigDecimal.valueOf(100));
        customer.setRegistrationDate(LocalDateTime.now());
        customer.setModificationDate(LocalDateTime.now());
        return customer;
    }
}
