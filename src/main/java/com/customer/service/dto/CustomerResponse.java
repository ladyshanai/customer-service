package com.customer.service.dto;

import java.math.BigDecimal;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastNameOrCompanyName,
        String documentNumber,
        String address,
        String phoneNumber,
        String email,
        String customerType,
        Boolean active,
        BigDecimal outstandingBalance
) {
}
