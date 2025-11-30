package com.ecom.order.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ecom.order.configs.FeignConfig;
import com.ecom.order.product.CancelStockRequest;
import com.ecom.order.product.ConfirmStockRequest;
import com.ecom.order.product.InventoryClient;
import com.ecom.order.product.ReserveStockRequest;
import com.ecom.order.product.ReserveStockResponse;
import com.ecom.order.product.StockException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryClient inventoryClient;

    public ReserveStockResponse reserveProducts(String orderId, Map<Long, Integer> products) {
        log.info("Reserving products for orderId={}, products={}", orderId, products);
        try {
            return inventoryClient.reserveProducts(new ReserveStockRequest(orderId, products));
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Inventory service not found: {}", e.getMessage());
            throw new StockException("Inventory service not available");
        } catch (FeignConfig.FeignBadRequestException e) {
            log.error("Invalid stock reservation request: {}", e.getMessage());
            throw new StockException("Invalid product reservation request: " + e.getMessage());
        } catch (FeignConfig.FeignServiceUnavailableException e) {
            log.error("Inventory service unavailable: {}", e.getMessage());
            throw new StockException("Inventory service is temporarily unavailable");
        } catch (FeignConfig.FeignServerException e) {
            log.error("Inventory service internal error: {}", e.getMessage());
            throw new StockException("Inventory service encountered an error");
        } catch (FeignException e) {
            log.error("Error reserving products: {}", e.getMessage());
            throw new StockException("Failed to reserve products");
        }
    }

    public void releaseReservation(String orderId) {
        log.info("Releasing stock reservation for orderId={}", orderId);
        try {
            inventoryClient.cancelStock(new CancelStockRequest(orderId));
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Reservation not found for orderId={}: {}", orderId, e.getMessage());
            log.warn("Reservation not found but continuing with order cancellation");
        } catch (FeignException e) {
            log.error("Error releasing reservation for orderId={}: {}", orderId, e.getMessage());
            log.warn("Failed to release reservation but continuing with order cancellation");
        }
    }

    public void confirmReservation(UUID orderId) {
        log.info("Confirming stock reservation for orderId={}", orderId);
        try {
            inventoryClient.confirmReservation(new ConfirmStockRequest(orderId.toString()));
        } catch (FeignConfig.FeignNotFoundException e) {
            log.error("Reservation not found for orderId={}: {}", orderId, e.getMessage());
            throw new StockException("Stock reservation not found");
        } catch (FeignConfig.FeignBadRequestException e) {
            log.error("Invalid confirmation request for orderId={}: {}", orderId, e.getMessage());
            throw new StockException("Cannot confirm reservation: " + e.getMessage());
        } catch (FeignConfig.FeignServiceUnavailableException e) {
            log.error("Inventory service unavailable for orderId={}: {}", orderId, e.getMessage());
            throw new StockException("Inventory service is temporarily unavailable");
        } catch (FeignException e) {
            log.error("Error confirming reservation for orderId={}: {}", orderId, e.getMessage());
            throw new StockException("Failed to confirm reservation");
        }
    }

}
