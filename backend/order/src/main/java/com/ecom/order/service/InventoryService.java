package com.ecom.order.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ecom.order.product.CancelStockRequest;
import com.ecom.order.product.ConfirmStockRequest;
import com.ecom.order.product.InventoryClient;
import com.ecom.order.product.ReserveStockRequest;
import com.ecom.order.product.ReserveStockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryClient inventoryClient;

    public ReserveStockResponse reserveProducts(String orderId, Map<Long, Integer> products) {
        log.info("Reserving products for orderId={}, products={}", orderId, products);
        return inventoryClient.reserveProducts(new ReserveStockRequest(orderId, products));
    }

    public void releaseReservation(String orderId) {
        log.info("Releasing stock reservation for orderId={}", orderId);
        inventoryClient.cancelStock(new CancelStockRequest(orderId));
    }

    public void confirmReservation(UUID orderId) {
        log.info("Confirming stock reservation for orderId={}", orderId);
        inventoryClient.confirmReservation(new ConfirmStockRequest(orderId.toString()));
    }

}
