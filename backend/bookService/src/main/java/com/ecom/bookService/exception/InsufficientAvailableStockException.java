package com.ecom.bookService.exception;

public class InsufficientAvailableStockException extends RuntimeException {
    public InsufficientAvailableStockException(String message) {
        super(message);
    }
}
