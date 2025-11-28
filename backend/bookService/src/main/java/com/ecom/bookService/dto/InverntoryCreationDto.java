package com.ecom.bookService.dto;

import com.ecom.bookService.model.Book;
import jakarta.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InverntoryCreationDto {

    // Book attirubuts
    private String title;
    private String author;
    private BigDecimal price;
    private String summary;
    private String isbn10;
    private String isbn13;
    private String thumbnail;
    private Integer publishedYear;
    private Integer numPages;

    // Inventory attributs
    private int availableQuantity;
    private int minimumStockLevel;
}
