package com.ecom.order.payment;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(
        name="payment-service",
        url = "${application.config.payment-url}"
)
public interface PaymentInterface {

    @PostMapping
    UUID requestOrderPayment(PaymentRequest paymentRequest);

}
