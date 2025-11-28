package com.ecom.bookService.dto;

import lombok.Data;

@Data
public class CreateInvetoryExistedBookDto {

    private Long bookId;
    private int availableQuantity;
    private int minimumStockLevel;
}
