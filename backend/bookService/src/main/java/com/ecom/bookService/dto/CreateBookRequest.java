package com.ecom.bookService.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.web.multipart.MultipartFile;

public record CreateBookRequest(
        @NotBlank String isbn10,
        String isbn13,
        @NotBlank String title,
        String description,
        @NotNull Long categoryId,
        @NotNull String author,
        @PositiveOrZero int initialStock,
        @Positive BigDecimal price
) {
}
