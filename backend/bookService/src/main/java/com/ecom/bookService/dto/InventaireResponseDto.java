package com.ecom.bookService.dto;

import com.ecom.bookService.model.Book;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
public class InventaireResponseDto {
    private Long id;

    private Long bookid;


    private int availableQuantity;

    private int reservedQuantity;

    private int minimumStockLevel;



    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;


}
