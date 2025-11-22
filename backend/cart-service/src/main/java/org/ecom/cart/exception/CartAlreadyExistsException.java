package org.ecom.cart.exception;

public class CartAlreadyExistsException extends RuntimeException {
    public CartAlreadyExistsException() {
    }

    public CartAlreadyExistsException(String message) {
        super(message);
    }

    public CartAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
