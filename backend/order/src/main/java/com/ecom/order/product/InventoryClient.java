package com.ecom.order.product;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecom.order.configs.FeignConfig;

@FeignClient(name = "bookService", configuration = FeignConfig.class)
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/reserve")
    ReserveStockResponse reserveProducts(@RequestBody ReserveStockRequest request);

    @PostMapping("/api/v1/inventory/cancel")
    void cancelStock(@RequestBody CancelStockRequest request);

}
