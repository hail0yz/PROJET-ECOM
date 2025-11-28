package com.ecom.bookService.mapper;


import com.ecom.bookService.dto.InventaireResponseDto;
import com.ecom.bookService.model.BookInventory;

public class BookInvetoryMapper {


    public static InventaireResponseDto toResponseInv(BookInventory binv){

        return InventaireResponseDto.builder()
                .id(binv.getId())
                .bookid(binv.getBook().getBookId())
                .availableQuantity(binv.getAvailableQuantity())
                .minimumStockLevel(binv.getMinimumStockLevel())
                .reservedQuantity(binv.getReservedQuantity())
                .updatedAt(binv.getUpdatedAt())
                .createdAt(binv.getCreatedAt())
                .build();
    }
}
