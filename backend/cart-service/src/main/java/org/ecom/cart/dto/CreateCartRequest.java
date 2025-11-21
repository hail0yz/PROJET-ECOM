package org.ecom.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public record CreateCartRequest(
        @NotNull List<CartItem> items
) {

    public record CartItem(
            Long productId,
            int quantity,
            BigDecimal price
    ) {
    }

}
