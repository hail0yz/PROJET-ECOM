package org.ecom.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecom.cart.dto.CartEntry;
import org.ecom.cart.dto.GetCartResponse;
import org.ecom.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get or create a customer's cart", tags = "Cart")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GetCartResponse.class))
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<GetCartResponse> getCartByUserId(@PathVariable String userId) {
        var response = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{userId}/items")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable String userId, @Valid @RequestBody CartEntry entry) {
        cartService.updateItemQuantity(userId, entry);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add an item to a user's cart", tags = "Cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Cart or product not found")
    })
    @PostMapping("/user/{userId}/items")
    public ResponseEntity<Void> addItemToCart(
            @PathVariable String userId,
            @RequestBody @Valid CartEntry entry
    ) {
        cartService.addItemToCart(userId, entry);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove an item from a user's cart", tags = "Cart")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Cart or product not found")
    })
    @DeleteMapping("/user/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable String userId,
            @Parameter(description = "ID of the product to remove") @PathVariable Long productId
    ) {
        cartService.removeItemFromCart(userId, productId);
        return ResponseEntity.ok().build();
    }

}
