package com.ecom.order.mapper;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.dto.PlaceOrderResponse;
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
                .cartId(order.getCartId())
                .customerId(order.getCustomerId())
                .lines(lines)
                .status(order.getStatus())
                .deliveryInfo(new OrderResponse.DeliveryInfo(
                        order.getDeliveryInfo().address1(),
                        order.getDeliveryInfo().address2(),
                        order.getDeliveryInfo().city(),
                        order.getDeliveryInfo().state(),
                        order.getDeliveryInfo().postalCode(),
                        order.getDeliveryInfo().country()
                ))
                .createdAt(order.getDate())
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

    public PlaceOrderResponse mapToPlaceOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return PlaceOrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .build();
    }

}
