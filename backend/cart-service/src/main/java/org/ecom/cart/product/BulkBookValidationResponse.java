package org.ecom.cart.product;

import java.math.BigDecimal;
import java.util.List;

public record BulkBookValidationResponse(
        boolean valid,
        List<BookValidationResult> items
) {

    public record BookValidationResult(
            Long bookId,
            String title,
            String image,
            boolean exists,
            Integer availableQuantity,
            Integer requestedQuantity,
            Integer inStock,
            BigDecimal price
    ) {
    }

}
