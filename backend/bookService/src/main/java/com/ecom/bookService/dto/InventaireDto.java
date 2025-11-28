package com.ecom.bookService.dto;


import com.ecom.bookService.model.Book;
import com.ecom.bookService.model.BookInventory;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class InventaireDto {

    private Long id;

    private Book book;


    private int availableQuantity;

    private int reservedQuantity;

    private int minimumStockLevel;


    @Version
    private Long version;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static InventaireDto toInvetaire(BookInventory bookInventory) {
        InventaireDto inventaireDto = new InventaireDto();
        inventaireDto.setId(bookInventory.getId());
        inventaireDto.setBook(bookInventory.getBook());
        inventaireDto.setAvailableQuantity(bookInventory.getAvailableQuantity());
        inventaireDto.setReservedQuantity(bookInventory.getReservedQuantity());
        inventaireDto.setMinimumStockLevel(bookInventory.getMinimumStockLevel());
        inventaireDto.setCreatedAt(bookInventory.getCreatedAt());
        inventaireDto.setUpdatedAt(bookInventory.getUpdatedAt());
        return inventaireDto;
    }
}
