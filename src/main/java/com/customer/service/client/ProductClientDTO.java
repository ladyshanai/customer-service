package com.customer.service.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductClientDTO(
        Long id,
        Long customerId,
        String productType,
        String productNumber,
        BigDecimal balance,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
