package com.ecom.order.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ecom.order.configs.FeignConfig;
import com.ecom.order.payment.CancelPaymentResponse;
import com.ecom.order.payment.CreatePaymentRequest;
import com.ecom.order.payment.PaymentClient;
import com.ecom.order.payment.PaymentFailedException;
import com.ecom.order.payment.PaymentResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            ResponseEntity<PaymentResponse> response = paymentClient.createPayment(request);

            if (!response.getStatusCode().isSameCodeAs(HttpStatus.CREATED)) {
                log.error("Payment creation failed with status: {}", response.getStatusCode());
                throw new PaymentFailedException("Payment creation failed with status: " + response.getStatusCode());
            }

            return response.getBody();
            
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Payment service not found: {}", e.getMessage());
            throw new PaymentFailedException("Payment service not available");
        } catch (FeignConfig.FeignServiceUnavailableException e) {
            log.error("Payment service unavailable: {}", e.getMessage());
            throw new PaymentFailedException("Payment service is temporarily unavailable");
        } catch (FeignConfig.FeignServerException e) {
            log.error("Payment service internal error: {}", e.getMessage());
            throw new PaymentFailedException("Payment service encountered an error");
        } catch (FeignException e) {
            log.error("Feign error during payment creation: {}", e.getMessage());
            throw new PaymentFailedException("Failed to communicate with payment service");
        }
    }

    public PaymentResponse syncPayment(Long paymentId) {
        try {
            ResponseEntity<PaymentResponse> response = paymentClient.syncPayment(paymentId);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PaymentFailedException("Payment sync failed");
            }

            return response.getBody();
            
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Payment not found: {}", e.getMessage());
            throw new PaymentFailedException("Payment not found");
        } catch (FeignException e) {
            log.error("Error syncing payment: {}", e.getMessage());
            throw new PaymentFailedException("Failed to sync payment");
        }
    }

    public void cancelPayment(Long paymentId) {
        try {
            ResponseEntity<CancelPaymentResponse> response = paymentClient.cancelPayment(paymentId);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Payment cancellation returned non-success status");
                throw new PaymentFailedException("Payment cancellation failed");
            }
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Payment not found for cancellation: {}", e.getMessage());
            throw new PaymentFailedException("Payment not found for cancellation", e);
        } catch (FeignConfig.FeignBadRequestException e) {
            log.error("Invalid payment cancellation request: {}", e.getMessage());
            throw new PaymentFailedException("Invalid payment cancellation request", e);
        } catch (FeignException e) {
            log.error("Error cancelling payment: {}", e.getMessage());
            throw new PaymentFailedException("Failed to cancel payment", e);
        }
    }

}