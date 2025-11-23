package com.ecom.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ecom.order.payment.CreatePaymentRequest;
import com.ecom.order.payment.PaymentClient;
import com.ecom.order.payment.PaymentFailedException;
import com.ecom.order.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        ResponseEntity<PaymentResponse> response = paymentClient.createPayment(request);

        if (!response.getStatusCode().isSameCodeAs(HttpStatus.CREATED)) {
            log.error("Payment creation failed with status: {}", response.getStatusCode());
            throw new PaymentFailedException();
        }

        if (response.getBody() == null) {
            throw new PaymentFailedException();
        }

        return response.getBody();
    }

    public PaymentResponse syncPayment(Long paymentId) {
        ResponseEntity<PaymentResponse> response = paymentClient.syncPayment(paymentId);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new PaymentFailedException();
        }

        return response.getBody();
    }

}