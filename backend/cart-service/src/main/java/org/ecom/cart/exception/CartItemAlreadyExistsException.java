package org.ecom.cart.exception;

public class CartItemAlreadyExistsException extends RuntimeException {
    public CartItemAlreadyExistsException() {
    }

    public CartItemAlreadyExistsException(String message) {
        super(message);
    }

    public CartItemAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
