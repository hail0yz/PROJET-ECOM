package com.ecom.order.payment;

public record CancelPaymentResponse(
    boolean canceled,
    String message
) {
}
