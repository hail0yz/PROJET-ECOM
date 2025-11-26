package org.ecom.cart.mapper;

import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.model.Cart;
import org.ecom.cart.model.CartItem;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CartMapperTest {

    private final CartMapper mapper = Mappers.getMapper(CartMapper.class);

    @Test
    void mapCart_nullSource_returnsNull() {
        GetCartResponse resp = mapper.mapCart(null);
        assertNull(resp);
    }

    @Test
    void mapCart_mapsFieldsAndItems_andComputesTotalPrice() {
        CartItem item1 = CartItem.builder()
                .productId(101L)
                .quantity(2)
                .price(new BigDecimal("10.50"))
                .image("/img1.png")
                .title("Product 1")
                .build();

        CartItem item2 = CartItem.builder()
                .productId(202L)
                .quantity(1)
                .price(new BigDecimal("5.00"))
                .image("/img2.png")
                .title("Product 2")
                .build();

        Cart cart = Cart.builder()
                .id(55L)
                .userId("user-1")
                .items(List.of(item1, item2))
                .build();

        GetCartResponse resp = mapper.mapCart(cart);

        assertEquals(55L, resp.getId());
        assertEquals("user-1", resp.getUserId());
        assertEquals(2, resp.getItems().size());

        GetCartResponse.CartItem ci1 = resp.getItems().get(0);
        assertEquals(101L, ci1.getProductId());
        assertEquals(2, ci1.getQuantity());
        assertEquals(new BigDecimal("10.50"), ci1.getPrice());
        assertEquals("/img1.png", ci1.getImage());
        assertEquals("Product 1", ci1.getTitle());

        // total price = 2 * 10.50 + 1 * 5.00 = 26.00
        assertEquals(new BigDecimal("26.00"), resp.getTotalPrice());
    }
}
