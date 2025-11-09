package com.ecom.bookService.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
    }

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

}