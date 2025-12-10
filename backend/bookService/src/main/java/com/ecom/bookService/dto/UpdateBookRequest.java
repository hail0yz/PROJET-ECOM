package com.ecom.bookService.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookRequest {

    @NotNull private Long categoryId;

    @NotBlank private String title;

    @NotBlank private String author;

    @PositiveOrZero private BigDecimal price;

    private Integer publishedYear;

    private Integer numPages;

    private String summary;

    private String thumbnail;

}
