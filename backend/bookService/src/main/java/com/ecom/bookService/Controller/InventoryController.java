package com.ecom.bookService.Controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.bookService.dto.CancelStockRequest;
import com.ecom.bookService.dto.ConfirmStockRequest;
import com.ecom.bookService.dto.ReservationResult;
import com.ecom.bookService.dto.ReserveStockRequest;
import com.ecom.bookService.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    // TODO receive stock

    // TODO adjust stock

    // TODO get out of stock items

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResult> reserveStock(@RequestBody @Valid ReserveStockRequest request) {
        ReservationResult result = inventoryService.reserveStock(request.orderId(), request.items());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmStock(@RequestBody @Valid ConfirmStockRequest request) {
        inventoryService.confirmReservation(request.orderId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelStock(@RequestBody @Valid CancelStockRequest request) {
        inventoryService.releaseReservation(request.orderId());
        return ResponseEntity.ok().build();
    }

}