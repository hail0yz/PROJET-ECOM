package org.ecom.cart;

import lombok.extern.slf4j.Slf4j;
import org.ecom.cart.exception.CartItemAlreadyExistsException;
import org.ecom.cart.exception.EntityNotFoundException;
import org.ecom.cart.exception.ProductDetailsInvalidException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = EntityNotFoundException.class)
    public ResponseEntity<?> handleException(EntityNotFoundException exception) {
        log.warn("Handling Entity not found", exception);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(exception = ProductDetailsInvalidException.class)
    public ResponseEntity<?> handleException(ProductDetailsInvalidException e) {
        log.warn("Handling Invalid product details in cart request", e);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }

    @ExceptionHandler(exception = CartItemAlreadyExistsException.class)
    public ResponseEntity<?> handleException(CartItemAlreadyExistsException e) {
        log.warn("Handling Cart item already exists", e);
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

}
