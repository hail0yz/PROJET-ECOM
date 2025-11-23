package com.ecom.order.mapper;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.order.dto.OrderLineRequest;
import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.model.OrderLine;
import com.ecom.order.repository.OrderRepo;

@Service
public class OrderLineMapper {

    @Autowired
    private OrderRepo orderRepo;

    public OrderLine toOrderLine(OrderLineRequest request) {

        return OrderLine.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .order(orderRepo.findById(request.getOrderId()).get())
                .build();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return new OrderLineResponse(
                orderLine.getOrderLineId(),
                orderLine.getQuantity(),
                orderLine.getProductId()
        );
    }
}
