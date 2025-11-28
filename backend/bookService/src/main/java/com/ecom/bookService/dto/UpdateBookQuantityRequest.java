package com.ecom.bookService.dto;

import jakarta.validation.constraints.Positive;

public record UpdateBookQuantityRequest(@Positive int quantity) {
}
