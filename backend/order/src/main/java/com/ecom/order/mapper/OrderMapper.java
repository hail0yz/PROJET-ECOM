package com.ecom.order.mapper;

import org.springframework.stereotype.Service;

import com.ecom.order.dto.OrderResponse;
import com.ecom.order.model.Order;

@Service
public class OrderMapper {

    public OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .reference(order.getReference())
                .amount(order.getTotalAmount())
//                .payment_method(order.getPaymentMethod())
                .customerId(order.getCustomerId())
                .build();
    }

}
