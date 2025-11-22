package org.ecom.customerservice.exception;

public class CustomerRegistrationFailedException extends RuntimeException {

    public CustomerRegistrationFailedException() {
    }

    public CustomerRegistrationFailedException(String message) {
        super(message);
    }

    public CustomerRegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
