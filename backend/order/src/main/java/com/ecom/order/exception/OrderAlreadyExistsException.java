package com.ecom.order.exception;

import java.util.UUID;

import lombok.Getter;

@Getter
public class OrderAlreadyExistsException extends RuntimeException {

    private final UUID orderId;

    public OrderAlreadyExistsException(UUID orderId) {
        super();
        this.orderId = orderId;
    }

    public OrderAlreadyExistsException(UUID orderId, String message) {
        super(message);
        this.orderId = orderId;
    }

}
