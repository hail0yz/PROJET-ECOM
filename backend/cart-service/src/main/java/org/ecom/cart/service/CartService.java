package org.ecom.cart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecom.cart.dto.CartEntry;
import org.ecom.cart.dto.CreateCartRequest;
import org.ecom.cart.dto.CreateCartResponse;
import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.exception.CartAlreadyExistsException;
import org.ecom.cart.exception.CartItemAlreadyExistsException;
import org.ecom.cart.exception.EntityNotFoundException;
import org.ecom.cart.exception.ProductDetailsInvalidException;
import org.ecom.cart.mapper.CartMapper;
import org.ecom.cart.model.Cart;
import org.ecom.cart.model.CartItem;
import org.ecom.cart.model.CartStatus;
import org.ecom.cart.product.BookClient;
import org.ecom.cart.product.BulkBookValidationRequest;
import org.ecom.cart.product.BulkBookValidationResponse;
import org.ecom.cart.repository.CartItemRepository;
import org.ecom.cart.repository.CartRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartMapper cartMapper;

    private final BookClient bookClient;

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

    public void completeCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found with ID: " + cartId));
        cart.setStatus(CartStatus.COMPLETE);
        cart.setResolvedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public CreateCartResponse createCart(CreateCartRequest request, String customerId) {
        log.info("Creating Cart customerId={}, Request={}", customerId, request);
        boolean exists = cartRepository.existsByUserIdAndStatus(customerId, CartStatus.OPEN);
        if (exists) {
            throw new CartAlreadyExistsException("Cart already exists for user " + customerId);
        }

        BulkBookValidationResponse bulkBookValidationResponse = validateProducts(request);

        if (!bulkBookValidationResponse.valid()) {
            log.error("Cart payload is not valid. Result = {}", bulkBookValidationResponse.items());
            throw new ProductDetailsInvalidException(bulkBookValidationResponse);
        }

        Cart cart = Cart.builder()
                .userId(customerId)
                .status(CartStatus.OPEN)
                .build();

        Cart savedCart = cartRepository.save(cart);


        List<CartItem> cartItems = bulkBookValidationResponse.items().stream()
                .map(item -> CartItem.builder()
                        .cart(savedCart)
                        .productId(item.bookId())
                        .title(item.title())
                        .image(item.image())
                        .quantity(item.requestedQuantity())
                        .price(item.price())
                        .build())
                .toList();
        cartItemRepository.saveAll(cartItems);

        log.info("Cart created customerId={} cartId={}", customerId, savedCart.getId());
        return CreateCartResponse.builder()
                .cartId(savedCart.getId())
                .build();
    }

    public boolean isCartOwner(Long cartId, String customerId) {
        return cartRepository.existsByIdAndUserId(cartId, customerId);
    }

    public boolean isCurrentCartOwner(String customerId) {
        return cartRepository.findByUserIdAndStatus(customerId, CartStatus.OPEN)
                .map(card -> customerId.equals(card.getUserId()))
                .orElse(true); // Since the shopping cart doesn't exist, we can say that the user has access, but in any case, they don't have a shopping cart.
    }

    public GetCartResponse getCartById(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        return cartMapper.mapCart(cart);
    }

    public GetCartResponse getCustomerCurrentCart(String customerId) {
        Cart cart = cartRepository.findByUserIdAndStatus(customerId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        return cartMapper.mapCart(cart);
    }

    public void addItemToCart(String userId, CartEntry entry) {
        log.info("Adding productId={} (quantity={}) to cart for userId={}",
                entry.productId(), entry.quantity(), userId);

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        addItemToCart(cart, entry);
    }

    public void addItemToCart(Long cartId, String userId, CartEntry entry) {
        log.info("Adding productId={} (quantity={}) to cart(cartId={}) for userId={}",
                entry.productId(), entry.quantity(), cartId, userId);

        Cart cart = cartRepository.findByIdAndUserIdAndStatus(cartId, userId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        addItemToCart(cart, entry);
    }

    public void addItemToCart(Cart cart, CartEntry entry) {
        Optional<CartItem> optional = cart.getItems().stream()
                .filter(cartItem -> entry.productId().equals(cartItem.getProductId()))
                .findFirst();

        if (optional.isPresent()) {
            throw new CartItemAlreadyExistsException("Book " + entry.productId() + " already added to cart");
        }

        BulkBookValidationResponse validationResponse = validateProduct(entry);

        if (!validationResponse.valid()) {
            log.error("Cart entry payload is not valid. Result = {}", validationResponse);
            throw new ProductDetailsInvalidException(validationResponse);
        }

        List<CartItem> cartItems = validationResponse.items().stream()
                .map(resultItem -> CartItem.builder()
                        .productId(resultItem.bookId())
                        .price(resultItem.price())
                        .title(resultItem.title())
                        .image(resultItem.image())
                        .quantity(resultItem.requestedQuantity())
                        .cart(cart)
                        .build())
                .toList();

        cartItemRepository.saveAll(cartItems);
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

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        removeItemFromCart(cart, productId);
    }

    public void removeItemFromCart(Long cartId, String userId, Long productId) {
        log.info("Removing productId={} from cart for userId={}", productId, userId);

        Cart cart = cartRepository.findByIdAndUserIdAndStatus(cartId, userId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        removeItemFromCart(cart, productId);
    }

    private void removeItemFromCart(Cart cart, Long productId) {
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        if (!removed) {
            throw new EntityNotFoundException("Item not found in cart: " + productId);
        }

        cartRepository.save(cart);

        log.info("Successfully removed productId={} from userId={} cart", productId, cart.getUserId());
    }

    public void clearCart(String userId) {
        log.info("Clearing cart for userId={}", userId);

        Cart cart = getCartByUser(userId);
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cleared cart for userId={}", userId);
    }

    public void clearCartById(Long cartId) {
        Cart cart = cartRepository.findByIdAndStatus(cartId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        List<CartItem> items = cart.getItems();
        cartItemRepository.deleteAll(items);
    }

    private Cart getCartByUser(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.OPEN)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));
    }

    private Cart createNewCart(String userId) {
        log.debug("Creating new Cart entity for userId={}", userId);
        return Cart.builder()
                .userId(userId)
                .build();
    }

    private BulkBookValidationResponse validateProducts(CreateCartRequest cartRequest) {
        BulkBookValidationRequest validationRequest = BulkBookValidationRequest.builder()
                .items(cartRequest.items().stream()
                        .map(cartItem -> BulkBookValidationRequest.BookValidationInput.builder()
                                .bookId(cartItem.productId())
                                .quantity(cartItem.quantity())
                                .build())
                        .toList())
                .build();

        return bookClient.validateProducts(validationRequest);
    }

    private BulkBookValidationResponse validateProduct(CartEntry entry) {
        BulkBookValidationRequest validationRequest = BulkBookValidationRequest.builder()
                .items(Stream.of(entry)
                        .map(cartItem -> BulkBookValidationRequest.BookValidationInput.builder()
                                .bookId(cartItem.productId())
                                .quantity(cartItem.quantity())
                                .build())
                        .toList())
                .build();

        return bookClient.validateProducts(validationRequest);
    }

}
