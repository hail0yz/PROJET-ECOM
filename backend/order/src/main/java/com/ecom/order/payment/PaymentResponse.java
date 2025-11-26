package com.ecom.order.payment;

import java.math.BigDecimal;

public record PaymentResponse(
        Long paymentId,
        String orderId,
        PaymentStatus status,
        String transactionId,
        String message,
        String failureReason,
        BigDecimal amount,
        String paymentMethod,
        String stripePaymentIntentId,
        String clientSecret
) {

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED,
        REQUIRES_ACTION
    }

}