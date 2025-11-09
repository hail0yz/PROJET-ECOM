package com.ecom.bookService.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelStockRequest(
        @NotBlank String orderId
) {}