package org.ecom.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCartResponse {

    private Long id;

    private String userId;

    private List<CartItem> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal totalPrice;

    @Data
    public static class CartItem {

        private Long productId;

        private int quantity;

        private double price;

        private String image;

        private String title;

    }

}
