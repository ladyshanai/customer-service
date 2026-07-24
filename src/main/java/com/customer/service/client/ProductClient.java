package com.customer.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/customer/{customerId}")
    List<ProductClientDTO> getProductsByCustomer(
            @PathVariable("customerId") Long customerId);

}
