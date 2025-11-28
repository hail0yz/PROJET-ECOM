package com.ecom.bookService.dto;


import lombok.Data;

@Data
public class UpdateInventaireDto {

    private Long inventaireId;

    private Long bookId;

    private int availableQuantity;

    private int reservedQuantity;

    private int minimumStockLevel;
}
