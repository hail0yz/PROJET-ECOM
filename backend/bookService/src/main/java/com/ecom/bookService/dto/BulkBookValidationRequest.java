package com.ecom.bookService.dto;

import java.util.List;

public record BulkBookValidationRequest(
        List<BookValidationInput> items
) {

    public record BookValidationInput(
            Long bookId,
            Integer quantity
    ) {

    }

}
