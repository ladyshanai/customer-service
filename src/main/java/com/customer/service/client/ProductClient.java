package com.customer.service.client;

import com.customer.service.config.ProductClientFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", configuration = ProductClientFeignConfig.class)
public interface ProductClient {

    @GetMapping("/api/products/customer/{customerId}")
    List<ProductClientDTO> getProductsByCustomer(
            @PathVariable("customerId") Long customerId);
}
