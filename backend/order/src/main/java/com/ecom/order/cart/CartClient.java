package com.ecom.order.cart;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/v1/carts/{cartId}")
    CartDetails getCartById(@PathVariable("cartId") String cartId);

}
