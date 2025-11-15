package com.ecom.order.cart;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", fallback = FallbackCartClient.class)
public interface CartClient {

    @GetMapping("/api/v1/carts/{cartId}")
    ResponseEntity<CartDetails> getCartById(@PathVariable("cartId") String cartId);

}
