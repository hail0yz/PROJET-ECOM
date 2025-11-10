package com.ecom.order.payment;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecom.order.configs.FeignConfig;

@FeignClient(name = "payment-service", configuration = FeignConfig.class)
public interface PaymentClient {

    @PostMapping("/api/payments")
    ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request);

}
