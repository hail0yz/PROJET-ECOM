package com.ecom.order.customer;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name="cutomer-service",url="${application.config.customer-url}")
public interface CutomerClient {

    @GetMapping("/{customer_id}")
    Optional<CustomerResponse> getCustomer(@PathVariable("customer_id") String customer_id);


}
