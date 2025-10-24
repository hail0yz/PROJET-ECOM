package com.ecom.order.controller;


import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.service.OrderLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order-lines")
public class OrderLineConroller {

    @Autowired
    private OrderLineService orderLineService;

    @GetMapping("/order/{order-id}")
    public ResponseEntity<List<OrderLineResponse>> getAllByOrderId(@PathVariable("order-id") UUID orderId) {
        return ResponseEntity.ok(orderLineService.findAllByOrderId(orderId));
    }
}
