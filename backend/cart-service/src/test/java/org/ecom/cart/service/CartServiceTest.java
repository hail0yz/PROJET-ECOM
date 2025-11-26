package org.ecom.cart.service;

import org.ecom.cart.dto.CartEntry;
import org.ecom.cart.dto.CreateCartRequest;
import org.ecom.cart.dto.CreateCartResponse;
import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.model.Cart;
import org.ecom.cart.model.CartItem;
import org.ecom.cart.product.BulkBookValidationResponse;
import org.ecom.cart.product.BulkBookValidationResponse.BookValidationResult;
import org.ecom.cart.product.BookClient;
import org.ecom.cart.repository.CartItemRepository;
import org.ecom.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    org.ecom.cart.mapper.CartMapper cartMapper;

    @Mock
    BookClient bookClient;

    @InjectMocks
    CartService cartService;

    private final String USER_ID = "user-1";

    @BeforeEach
    void setUp() {
    }

    @Test
    void getOrCreateCart_whenExists_returnsMappedResponse() {
        Cart cart = Cart.builder().id(10L).userId(USER_ID).build();
        GetCartResponse resp = GetCartResponse.builder().id(10L).userId(USER_ID).build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartMapper.mapCart(cart)).thenReturn(resp);

        GetCartResponse result = cartService.getOrCreateCart(USER_ID);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getOrCreateCart_whenNotExists_createsAndReturnsMappedResponse() {
        Cart saved = Cart.builder().id(11L).userId(USER_ID).build();
        GetCartResponse resp = GetCartResponse.builder().id(11L).userId(USER_ID).build();

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);
        when(cartMapper.mapCart(saved)).thenReturn(resp);

        GetCartResponse result = cartService.getOrCreateCart(USER_ID);

        assertNotNull(result);
        assertEquals(11L, result.getId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void createCart_whenCartExists_throws() {
        CreateCartRequest req = new CreateCartRequest(List.of());
        when(cartRepository.existsByUserIdAndStatus(eq(USER_ID), any())).thenReturn(true);

        assertThrows(org.ecom.cart.exception.CartAlreadyExistsException.class,
                () -> cartService.createCart(req, USER_ID));
    }

    @Test
    void createCart_whenValidationFails_throws() {
        CreateCartRequest req = new CreateCartRequest(List.of());
        when(cartRepository.existsByUserIdAndStatus(eq(USER_ID), any())).thenReturn(false);

        BulkBookValidationResponse invalid = new BulkBookValidationResponse(false, List.of());
        when(bookClient.validateProducts(any())).thenReturn(invalid);

        assertThrows(org.ecom.cart.exception.ProductDetailsInvalidException.class,
                () -> cartService.createCart(req, USER_ID));
    }

    @Test
    void createCart_success_savesCartAndItems_returnsCartId() {
        var itemRes = new BookValidationResult(101L, "T", "/img", true, 10, 2, 8, new BigDecimal("9.99"));
        BulkBookValidationResponse valid = new BulkBookValidationResponse(true, List.of(itemRes));

        CreateCartRequest req = new CreateCartRequest(List.of(new org.ecom.cart.dto.CreateCartRequest.CartItem(101L, 2, new BigDecimal("9.99"))));

        when(cartRepository.existsByUserIdAndStatus(eq(USER_ID), any())).thenReturn(false);
        when(bookClient.validateProducts(any())).thenReturn(valid);

        Cart saved = Cart.builder().id(5L).userId(USER_ID).build();
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        CreateCartResponse resp = cartService.createCart(req, USER_ID);

        assertNotNull(resp);
        assertEquals(5L, resp.getCartId());
        verify(cartItemRepository).saveAll(any());
    }

    @Test
    void addItemToCart_whenItemAlreadyExists_throws() {
        Cart cart = Cart.builder().id(1L).userId(USER_ID).build();
        CartItem existing = CartItem.builder().productId(200L).quantity(1).build();
        cart.getItems().add(existing);

        CartEntry entry = new CartEntry(200L, 1);

        assertThrows(org.ecom.cart.exception.CartItemAlreadyExistsException.class,
                () -> cartService.addItemToCart(cart, entry));
    }

    @Test
    void addItemToCart_valid_savesItems() {
        Cart cart = Cart.builder().id(2L).userId(USER_ID).build();
        CartEntry entry = new CartEntry(300L, 1);

        var resItem = new BookValidationResult(300L, "Title", "/img", true, 5, 1, 4, new BigDecimal("7.50"));
        BulkBookValidationResponse valid = new BulkBookValidationResponse(true, List.of(resItem));

        when(bookClient.validateProducts(any())).thenReturn(valid);

        cartService.addItemToCart(cart, entry);

        verify(cartItemRepository).saveAll(any());
    }

    @Test
    void removeItemFromCart_itemNotFound_throws() {
        Cart cart = Cart.builder().id(3L).userId(USER_ID).build();
        when(cartRepository.findByUserIdAndStatus(eq(USER_ID), any())).thenReturn(Optional.of(cart));

        assertThrows(org.ecom.cart.exception.EntityNotFoundException.class,
                () -> cartService.removeItemFromCart(USER_ID, 999L));
    }

    @Test
    void removeItemFromCart_success_removesAndSaves() {
        Cart cart = Cart.builder().id(4L).userId(USER_ID).build();
        CartItem item = CartItem.builder().productId(400L).quantity(1).build();
        cart.getItems().add(item);

        when(cartRepository.findByUserIdAndStatus(eq(USER_ID), any())).thenReturn(Optional.of(cart));

        cartService.removeItemFromCart(USER_ID, 400L);

        verify(cartRepository).save(cart);
        assertTrue(cart.getItems().isEmpty());
    }
}
