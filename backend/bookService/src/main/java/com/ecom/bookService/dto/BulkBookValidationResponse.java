package com.ecom.bookService.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkBookValidationResponse {

    private boolean valid;
    private List<BookValidationResult> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookValidationResult {
        private Long bookId;
        private boolean exists;
        private String title;
        private String image;
        private Integer availableQuantity;
        private Integer requestedQuantity;
        private Integer inStock;
        private BigDecimal price;
    }

}
