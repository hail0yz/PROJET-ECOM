package com.ecom.order.controller;


import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.dto.PlaceOrderResponse;
import com.ecom.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OrderController {


    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @RequestBody @Valid OrderRequest request,
            @AuthenticationPrincipal(expression = "subject") String customerId
    ) {
        return ResponseEntity.ok(this.orderService.placeOrder(request, customerId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(this.orderService.findAllOrders());
    }

    @PreAuthorize("@orderService.isOrderOwner(#orderId, authentication.principal.getClaim('sub'))")
    @GetMapping("/{order-id}")
    public ResponseEntity<OrderResponse> findById(
            @PathVariable("order-id") UUID orderId
    ) {
        return ResponseEntity.ok(this.orderService.findById(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(this.orderService.getCustomerOrders(customerId, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal(expression = "subject") String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(this.orderService.getCustomerOrders(customerId, page, size));
    }

    @PreAuthorize("@orderService.isOrderOwner(#orderId, authentication.principal.getClaim('sub'))")
    @PostMapping("/{order-id}/confirm-payment")
    public ResponseEntity<com.ecom.order.payment.PaymentResponse> confirmPayment(
            @PathVariable("order-id") UUID orderId
    ) {
        return ResponseEntity.ok(this.orderService.confirmOrderPayment(orderId));
    }

}
