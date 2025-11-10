package com.ecom.order.model;

public enum OrderStatus {
    PENDING,
    VALIDATED,
    RESERVED,
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED,
    RETURN_REQUESTED,
    RETURNED
}
