package org.ecom.cart.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.ecom.cart.config.FeignConfig;

@FeignClient(name = "bookService", configuration = FeignConfig.class)
public interface BookClient {

    @PostMapping("/api/v1/books/validate")
    BulkBookValidationResponse validateProducts(@RequestBody BulkBookValidationRequest request);

}
