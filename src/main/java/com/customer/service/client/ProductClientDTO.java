package com.customer.service.client;

import java.math.BigDecimal;

public record ProductClientDTO(
        Long id,
        Long customerId,
        String productType,
        String productNumber,
        BigDecimal balance,
        Boolean active

) {
}
