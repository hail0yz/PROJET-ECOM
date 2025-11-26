package com.ecom.order.service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ecom.order.cart.CartDetails;
import com.ecom.order.customer.CustomerDetails;
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
import com.ecom.order.payment.PaymentResponse;
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

    private final CustomerService customerService;

    private final OrderMapper orderMapper;

    private final CartService cartService;

    private final InventoryService inventoryService;

    private final PaymentService paymentService;

    private final Executor taskExecutor;

    @Transactional
    public PlaceOrderResponse placeOrder(OrderRequest orderRequest, String customerId) {
        log.info("Placing order customerId={}, request={}", customerId, orderRequest);

        CompletableFuture<CustomerDetails> customerFuture = CompletableFuture
                .supplyAsync(() -> customerService.getCustomerDetails(customerId), taskExecutor);

        CompletableFuture<CartDetails> cartFuture = CompletableFuture
                .supplyAsync(() -> cartService.getCartById(orderRequest.getCartId()), taskExecutor);

        CompletableFuture.allOf(customerFuture, cartFuture).join();

        CustomerDetails customer = customerFuture.join();
        CartDetails cart = cartFuture.join();

        Order order = orderRepo.save(createOrderFromCart(orderRequest, customer, cart));

        if (!reserveStockAndHandleFailure(cart, order)) {
            return PlaceOrderResponse.builder()
                    .orderId(order.getId())
                    .orderStatus(order.getStatus())
                    .build();
        }

        var paymentDetails = processOrderPayment(customer, order, cart);

        if (paymentDetails == null) {
            CompletableFuture.runAsync(
                    () -> inventoryService.releaseReservation(order.getId().toString()),
                    taskExecutor
            );
        }
        else {
            CompletableFuture.runAsync(() -> cartService.completeCart(cart.id()), taskExecutor);
        }

        // TODO publish event OrderPlaced

        return PlaceOrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .paymentDetails(paymentDetails)
                .build();
    }

    private PlaceOrderResponse.PaymentDetails processOrderPayment(CustomerDetails customer, Order order, CartDetails cart) {
        log.info("Process order payment for orderId={}", order.getId());
        BigDecimal total = cart.items().stream()
                .map(CartDetails.CartItem::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .paymentMethod(order.getPaymentInfo().getPaymentMethod())
                .customerId(customer.id())
                .customerEmail(customer.email())
                .orderId(order.getId().toString())
                .amount(total)
                .build();

        try {
            PaymentResponse paymentResponse = paymentService.createPayment(createPaymentRequest);
            order.setStatus(OrderStatus.PAYMENT_PENDING);
            order.getPaymentInfo().setPaymentId(paymentResponse.paymentId());

            orderRepo.save(order);

            return PlaceOrderResponse.PaymentDetails.builder()
                    .paymentId(paymentResponse.paymentId())
                    .paymentStatus(paymentResponse.status().toString())
                    .stripePaymentIntentId(paymentResponse.stripePaymentIntentId())
                    .clientSecret(paymentResponse.clientSecret())
                    .transactionId(paymentResponse.transactionId())
                    .build();
        }
        catch (PaymentFailedException exception) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            return null;
        }
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
            orderRepo.save(order);
            return false;
        }

        return true;
    }

    private Order createOrderFromCart(OrderRequest request, CustomerDetails customer, CartDetails cart) {
        List<OrderLine> orderLines = cart.items().stream()
                .map(item -> OrderLine.builder()
                        .quantity(item.quantity())
                        .productId(item.productId())
                        .build())
                .collect(Collectors.toList());

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
                .customerId(customer.id())
                .cartId(cart.id())
                .orderLines(orderLines)
                .totalAmount(cart.totalPrice())
                .paymentInfo(paymentInfo)
                .deliveryInfo(deliveryInfo)
                .totalAmount(cart.totalPrice())
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

    public PaymentResponse confirmOrderPayment(UUID orderId) {
        log.info("Confirming order payment for orderId={}", orderId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        PaymentInfo paymentInfo = order.getPaymentInfo();
        if (paymentInfo == null || paymentInfo.getPaymentId() == null) {
            throw new EntityNotFoundException("No payment associated with order: " + orderId);
        }

        PaymentResponse paymentResponse = this.paymentService.syncPayment(paymentInfo.getPaymentId());

        log.info("Payment response received after [payment-sync]: {}", paymentResponse);

        switch (paymentResponse.status()) {
            case COMPLETED:
                order.setStatus(OrderStatus.CONFIRMED);
                break;
            case PROCESSING:
            case PENDING:
            case REQUIRES_ACTION:
                order.setStatus(OrderStatus.PAYMENT_PENDING);
                break;
            case REFUNDED:
                order.setStatus(OrderStatus.REFUNDED);
                break;
            case CANCELLED:
                order.setStatus(OrderStatus.CANCELLED);
                break;
            default:
                order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepo.save(order);

        CompletableFuture.runAsync(() -> inventoryService.confirmReservation(order.getId()), taskExecutor);

        return paymentResponse;
    }

    public boolean isOrderOwner(UUID orderId, String customerId) {
        return orderRepo.findById(orderId)
                .map(order -> customerId.equals(order.getCustomerId()))
                .orElse(true);
    }

}
