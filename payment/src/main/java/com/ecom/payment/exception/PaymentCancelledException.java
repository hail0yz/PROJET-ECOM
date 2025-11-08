package com.ecom.payment.exception;

public class PaymentCancelledException extends RuntimeException {
    public PaymentCancelledException(String message) {
        super(message);
    }
}