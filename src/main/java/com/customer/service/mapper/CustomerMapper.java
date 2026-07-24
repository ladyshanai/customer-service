package com.customer.service.mapper;

import com.customer.service.client.ProductClientDTO;
import com.customer.service.dto.CustomerDetailResponse;
import com.customer.service.dto.CustomerRequest;
import com.customer.service.dto.CustomerResponse;
import com.customer.service.entity.CustomerEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    CustomerEntity toEntity(CustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CustomerRequest request,
                                 @MappingTarget CustomerEntity customer);

    CustomerResponse toResponse(CustomerEntity customer);

    CustomerDetailResponse toDetailResponse(
            CustomerEntity customer,
            List<ProductClientDTO> products
    );

    @AfterMapping
    default void setDefaults(CustomerRequest request,
                             @MappingTarget CustomerEntity customer) {

        if (customer.getOutstandingBalance() == null) {
            customer.setOutstandingBalance(BigDecimal.ZERO);
        }

        if (customer.getActive() == null) {
            customer.setActive(true);
        }

        if (customer.getRegistrationDate() == null) {
            customer.setRegistrationDate(LocalDateTime.now());
        }

        customer.setModificationDate(LocalDateTime.now());
    }

}
