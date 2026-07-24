package com.customer.service.dto;

import com.customer.service.client.ProductClientDTO;

import java.math.BigDecimal;
import java.util.List;

public record CustomerDetailResponse(
        Long id,
        String firstName,
        String lastNameOrCompanyName,
        String documentNumber,
        String address,
        String phoneNumber,
        String email,
        String customerType,
        Boolean active,
        BigDecimal outstandingBalance,
        List<ProductClientDTO> products
) {
}
