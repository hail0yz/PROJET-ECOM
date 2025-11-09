package com.ecom.order.service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.order.cart.CartClient;
import com.ecom.order.cart.CartDetails;
import com.ecom.order.customer.CustomerResponse;
import com.ecom.order.customer.CutomerClient;
import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.exception.BusinessException;
import com.ecom.order.kafka.OrderProducer;
import com.ecom.order.mapper.OrderMapper;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderLine;
import com.ecom.order.model.OrderStatus;
import com.ecom.order.payment.PaymentInterface;
import com.ecom.order.product.InventoryClient;
import com.ecom.order.product.ReserveStockRequest;
import com.ecom.order.product.ReserveStockResponse;
import com.ecom.order.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepo orderRepo;

    private final CutomerClient customerClient;

    private final OrderMapper orderMapper;

    private final OrderProducer orderProducer;

    private final PaymentInterface paymentInterface;

    private final CartClient cartClient;

    private final InventoryClient inventoryClient;

    public UUID placeOrder(OrderRequest orderRequest) {
        String customerId = "CUSTOMER_ID"; // extracted from 'Authorization Header' token
        CustomerResponse customer = customerClient.getCustomer(customerId)
                .orElseThrow(() -> new BusinessException("Customer not found"));

        CartDetails cart = cartClient.getCartById(orderRequest.getCartId());

        Order order = orderRepo.save(buildOrder(customerId, cart.items()));

        ReserveStockResponse reserveStockResponse = reserveProducts(order.getId().toString(), cart.items());

        if (!reserveStockResponse.success()) {
            log.error("Reserving stock failed [orderId={}]: {}", order.getId(), reserveStockResponse.message());
            order.setStatus(OrderStatus.FAILED);
            return order.getId();
        }

        // TODO payment

//        var paymentRequest = new PaymentRequest(
//                order.getReference(),
//                order.getId(),
//                orderRequest.getPaymentMethod(),
//                orderRequest.getAmmount(),
//                customer
//        );
//
//        paymentInterface.requestOrderPayment(paymentRequest);
//
//        //send orderconfirmation to notification microservice
//        OrderConfirmation orderConfirmation = new OrderConfirmation(
//                orderRequest.getReference(),
//                orderRequest.getAmmount(),
//                orderRequest.getPaymentMethod(),
//                customer,
//                products
//        );

//        orderProducer.sentOrderConfirmation(orderConfirmation);
        return order.getId();
    }

    private Order buildOrder(String customerId, List<CartDetails.CartItem> items) {
        List<OrderLine> orderLines = items.stream()
                .map(item -> OrderLine.builder()
                        .quantity((long) item.quantity())
                        .productId(item.productId())
                        .build())
                .toList();

        return Order.builder()
                .customerId(customerId)
                .orderLines(orderLines)
                .totalAmount(BigDecimal.valueOf(0.0))
                .paymentInfo(null) // TODO set payment info
                .deliveryInfo(null) // TODO delivery info
                .build();
    }

    public Page<OrderResponse> getCustomerOrders(String customerId, int page, int size) {
        return orderRepo.findByCustomerId(customerId, PageRequest.of(page, size))
                .map(orderMapper::fromOrder);
    }

    public List<OrderResponse> findAllOrders() {
        return this.orderRepo.findAll()
                .stream()
                .map(this.orderMapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(UUID id) {
        return this.orderRepo.findById(id)
                .map(this.orderMapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }

    private ReserveStockResponse reserveProducts(String orderId, List<CartDetails.CartItem> items) {
        Map<Long, Integer> products = items.stream()
                .collect(Collectors.toMap(
                        CartDetails.CartItem::productId,
                        CartDetails.CartItem::quantity
                ));

        return inventoryClient.reserveProducts(new ReserveStockRequest(orderId, products));
    }

}
