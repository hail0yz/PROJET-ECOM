package com.ecom.order.mapper;

import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public Order toOrder(OrderRequest orderRequest) {
        return Order.builder()
                .reference(orderRequest.getReference())
                .paymentMethod(orderRequest.getPaymentMethod())
                .totalAmount(orderRequest.getAmmount())
                .customerId(orderRequest.getCustomerId())
                .build();
    }

    public OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .reference(order.getReference())
                .amount(order.getTotalAmount())
                .payment_method(order.getPaymentMethod())
                .customer_id(order.getCustomerId())
                .build();
    }
}
