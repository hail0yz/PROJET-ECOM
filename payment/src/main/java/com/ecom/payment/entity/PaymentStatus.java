package com.ecom.payment.entity;
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED, REQUIRES_ACTION
}