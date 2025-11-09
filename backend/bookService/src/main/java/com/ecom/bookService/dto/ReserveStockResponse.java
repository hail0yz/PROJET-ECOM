package com.ecom.bookService.dto;

import lombok.Data;

@Data
public class ReserveStockResponse {
    private boolean success;
    private String reservationId;
    private String message;
    private Integer availableQuantity;
}