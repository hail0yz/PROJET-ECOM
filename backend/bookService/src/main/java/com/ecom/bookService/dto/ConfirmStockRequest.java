package com.ecom.bookService.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmStockRequest(@NotBlank String orderId) {}