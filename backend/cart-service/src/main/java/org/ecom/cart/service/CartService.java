package org.ecom.cart.service;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.cart.dto.CartEntry;
import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.exception.EntityNotFoundException;
import org.ecom.cart.mapper.CartMapper;
import org.ecom.cart.model.Cart;
import org.ecom.cart.model.CartItem;
import org.ecom.cart.repository.CartItemRepository;
import org.ecom.cart.repository.CartRepository;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartMapper cartMapper;

    /**
     * Finds an open cart for the given user, or creates a new one if none exists.
     */
    public GetCartResponse getOrCreateCart(String userId) {
        log.debug("Fetching cart for userId={}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No cart found for user id: {}, creating new one", userId);
                    return cartRepository.save(createNewCart(userId));
                });

        log.debug("Cart retrieved successfully for userId={}, cartId={}", userId, cart.getId());
        return cartMapper.mapCart(cart);
    }

    public void addItemToCart(String userId, CartEntry entry) {
        log.info("Adding productId={} (quantity={}) to cart for userId={}",
                entry.productId(), entry.quantity(), userId);

        // TODO
    }

    public void updateItemQuantity(String userId, CartEntry entry) {
        log.info("Updating quantity for productId={} in userId={} cart to {}",
                entry.productId(), userId, entry.quantity());

        Cart cart = getCartByUser(userId);

        CartItem item = cartItemRepository.findByProductIdAndCartId(entry.productId(), cart.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cart Item not found in cart: " + entry.productId()));

        item.setQuantity(entry.quantity());

        cartRepository.save(cart);

        log.info("Updated productId={} quantity to {} in userId={} cart",
                entry.productId(), entry.quantity(), userId);
    }

    public void removeItemFromCart(String userId, Long productId) {
        log.info("Removing productId={} from cart for userId={}", productId, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new EntityNotFoundException("Item not found in cart: " + productId);
        }

        cartRepository.save(cart);

        log.info("Successfully removed productId={} from userId={} cart", productId, userId);
    }

    public void clearCart(String userId) {
        log.info("Clearing cart for userId={}", userId);

        Cart cart = getCartByUser(userId);
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cleared cart for userId={}", userId);
    }

    private Cart getCartByUser(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));
    }

    private Cart createNewCart(String userId) {
        log.debug("Creating new Cart entity for userId={}", userId);
        return Cart.builder()
                .userId(userId)
                .build();
    }

}
