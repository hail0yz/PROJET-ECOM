package com.ecom.order.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartDetails(
        Long id,
        String userId,
        List<CartItem> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal totalPrice
) {

    public record CartItem(
        Long productId,
        int quantity,
        double price
    ) {
    }

}
