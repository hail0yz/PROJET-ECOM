package com.ecom.order.customer;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

import com.ecom.order.configs.FeignConfig;

@FeignClient(name = "customer-service", configuration = FeignConfig.class)
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{customerId}")
    Optional<CustomerResponse> getCustomer(@PathVariable String customerId);

}
