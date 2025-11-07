package com.ecom.payment.exception;

public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}