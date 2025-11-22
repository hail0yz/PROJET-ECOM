package com.ecom.order.exception;

public class BadREquestException extends RuntimeException {

    public BadREquestException() {
    }

    public BadREquestException(String message) {
        super(message);
    }

    public BadREquestException(String message, Throwable cause) {
        super(message, cause);
    }

}
