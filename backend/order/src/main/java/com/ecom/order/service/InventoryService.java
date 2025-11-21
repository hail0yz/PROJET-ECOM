package com.ecom.order.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ecom.order.product.CancelStockRequest;
import com.ecom.order.product.InventoryClient;
import com.ecom.order.product.ReserveStockRequest;
import com.ecom.order.product.ReserveStockResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryClient inventoryClient;

    public ReserveStockResponse reserveProducts(String orderId, Map<Long, Integer> products) {
        return inventoryClient.reserveProducts(new ReserveStockRequest(orderId, products));
    }

    public void releaseReservation(String orderId) {
        inventoryClient.cancelStock(new CancelStockRequest(orderId));
    }

}
