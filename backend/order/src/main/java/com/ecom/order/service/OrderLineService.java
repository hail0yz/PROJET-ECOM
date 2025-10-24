package com.ecom.order.service;


import com.ecom.order.dto.OrderLineRequest;
import com.ecom.order.dto.OrderLineResponse;
import com.ecom.order.mapper.OrderLineMapper;
import com.ecom.order.model.OrderLine;
import com.ecom.order.repository.OrderLineRepo;
import com.ecom.order.repository.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderLineService {

    @Autowired
    private OrderLineMapper orderLineMapper;
    @Autowired
    private OrderLineRepo orderLineRepo;


    public UUID saveOrderLine(OrderLineRequest request) {
        OrderLine orderLine=orderLineMapper.toOrderLine(request);
        return  orderLineRepo.save(orderLine).getOrderLineId();
    }

    public List<OrderLineResponse> findAllByOrderId(UUID orderId) {
        return orderLineRepo.findAllByOrderId(orderId)
                .stream()
                .map(orderLineMapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }
}
