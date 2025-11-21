package org.ecom.cart.product;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkBookValidationRequest {

    private List<BookValidationInput> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookValidationInput {
        private Long bookId;
        private Integer quantity;
    }

}
