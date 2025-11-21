package com.ecom.order.customer;


import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ecom.order.configs.FeignConfig;

@FeignClient(name = "customer-service", configuration = FeignConfig.class)
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{customerId}")
    Optional<CustomerResponse> getCustomer(@PathVariable String customerId);

    @GetMapping("/api/v1/customers/{customerId}/details")
    ResponseEntity<CustomerDetails> getCustomerDetails(@PathVariable String customerId);

}
