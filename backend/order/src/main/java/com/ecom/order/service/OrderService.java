package com.ecom.order.service;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.order.cart.CartClient;
import com.ecom.order.cart.CartDetails;
import com.ecom.order.customer.CustomerResponse;
import com.ecom.order.customer.CustomerClient;
import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.dto.PlaceOrderResponse;
import com.ecom.order.exception.EntityNotFoundException;
import com.ecom.order.mapper.OrderMapper;
import com.ecom.order.model.DeliveryInfo;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderLine;
import com.ecom.order.model.OrderStatus;
import com.ecom.order.model.PaymentInfo;
import com.ecom.order.payment.CreatePaymentRequest;
import com.ecom.order.payment.PaymentFailedException;
import com.ecom.order.product.ReserveStockResponse;
import com.ecom.order.repository.OrderRepo;
import static java.util.stream.Collectors.toMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepo orderRepo;

    private final CustomerClient customerClient;

    private final OrderMapper orderMapper;

    private final CartClient cartClient;

    private final InventoryService inventoryService;

    private final PaymentService paymentService;

    public PlaceOrderResponse placeOrder(OrderRequest orderRequest) {
        String customerId = "CUSTOMER_ID"; // extracted from 'Authorization Header' token
        CustomerResponse customer = customerClient.getCustomer(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        CartDetails cart = cartClient.getCartById(orderRequest.getCartId());

        Order order = orderRepo.save(createOrderFromCart(orderRequest, customer, cart));

        if (!reserveStockAndHandleFailure(cart, order)) {
            return PlaceOrderResponse.builder()
                    .orderId(order.getId())
                    .build();
        }

        boolean paymentFailed = !processOrderPayment(customer, order, cart);

        if (paymentFailed) {
            // TODO cancel / release books reservation
            // InventoryService.releaseReservation(...)
        }

        // TODO publish event OrderPlaced

        return PlaceOrderResponse.builder()
                .orderId(order.getId())
                .paymentId(order.getPaymentInfo().paymentId())
                .build();
    }

    private boolean processOrderPayment(CustomerResponse customer, Order order, CartDetails cart) {
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .paymentMethod(order.getPaymentInfo().paymentMethod())
                .customerEmail(customer.getEmail())
                .orderId(order.getId().toString())
                .amount(cart.totalPrice())
                .build();

        try {
            Long paymentId = paymentService.createPayment(createPaymentRequest);
            order.setStatus(OrderStatus.PAYMENT_PENDING);
            order.setPaymentInfo(order.getPaymentInfo().withPaymentId(paymentId));
        }
        catch (PaymentFailedException exception) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            return false;
        }

        return true;
    }

    private boolean reserveStockAndHandleFailure(CartDetails cart, Order order) {
        Map<Long, Integer> productsWithQuantities = cart.items().stream().collect(toMap(
                CartDetails.CartItem::productId,
                CartDetails.CartItem::quantity
        ));
        ReserveStockResponse reserveStockResponse = inventoryService.reserveProducts(order.getId().toString(), productsWithQuantities);

        if (!reserveStockResponse.success()) {
            log.error("Reserving stock failed [orderId={}]: {}", order.getId(), reserveStockResponse.message());
            order.setStatus(OrderStatus.FAILED);
            return false;
        }
        return true;
    }

    private Order createOrderFromCart(OrderRequest request, CustomerResponse customer, CartDetails cart) {
        List<OrderLine> orderLines = cart.items().stream()
                .map(item -> OrderLine.builder()
                        .quantity(item.quantity())
                        .productId(item.productId())
                        .build())
                .toList();

        PaymentInfo paymentInfo = new PaymentInfo(null, request.getPaymentDetails().paymentMethod());
        DeliveryInfo deliveryInfo = new DeliveryInfo(
                request.getAddress().street(),
                null,
                request.getAddress().city(),
                null,
                request.getAddress().postalCode(),
                request.getAddress().country()
        );
        return Order.builder()
                .customerId(customer.getCutomerId())
                .orderLines(orderLines)
                .totalAmount(cart.totalPrice())
                .paymentInfo(paymentInfo)
                .deliveryInfo(deliveryInfo)
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
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: " + id)));
    }

}
