package com.ecom.bookService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {

    private Long id;

    private CategoryDTO category;

    private String title;

    private String author;

    private BigDecimal price;

    private int stock;

    private String summary;

    private String isbn10;

    private String isbn13;

    private String thumbnail;

    private Integer publishedYear;

    private Integer numPages;

    private LocalDateTime createdAt;

}
