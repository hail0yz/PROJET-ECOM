package com.ecom.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ecom.order.customer.CustomerClient;
import com.ecom.order.customer.CustomerDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerClient customerClient;

    public CustomerDetails getCustomerDetails(String customerId) {
        log.info("Getting customer details (customerId={})", customerId);
        ResponseEntity<CustomerDetails> response = customerClient.getCustomerDetails(customerId);
        if (!response.getStatusCode().isSameCodeAs(HttpStatus.OK) || !response.hasBody()) {
            log.error("Failed to retrieve customer details. Response={}", response);
            throw new RuntimeException("Failed to retrieve customer details");
        }

        return response.getBody();
    }

}
