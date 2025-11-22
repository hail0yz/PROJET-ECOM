package com.ecom.order.mapper;

import org.springframework.stereotype.Service;

import com.ecom.order.dto.OrderResponse;
import com.ecom.order.model.Order;

@Service
public class OrderMapper {

    public OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId().toString())
                .amount(order.getTotalAmount())
                .customerId(order.getCustomerId())
                //.payment_method(order.getPaymentInfo() != null ? order.getPaymentInfo().paymentMethod() : null)
                .build();
    }

}
