package com.ecom.order.mapper;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderLine;

@Service
public class OrderMapper {

    public OrderResponse fromOrder(Order order) {
        if (order == null) return null;

        List<OrderLineResponse> lines = order.getOrderLines().stream()
                .map(this::toOrderLineResponse)
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId().toString())
                .amount(order.getTotalAmount())
                .customerId(order.getCustomerId())
                .lines(lines)
                //.payment_method(order.getPaymentInfo() != null ? order.getPaymentInfo().paymentMethod() : null)
                .build();
    }

    private OrderLineResponse toOrderLineResponse(OrderLine line) {
        if (line == null) return null;

        return OrderLineResponse.builder()
                .id(line.getOrderLineId())
                .productId(line.getProductId())
                .quantity(line.getQuantity())
                .build();
    }

}
