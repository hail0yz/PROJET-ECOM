package com.ecom.order.service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.order.cart.CartDetails;
import com.ecom.order.customer.CustomerDetails;
import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.OrderResponse;
import com.ecom.order.dto.OrderStatsResponse;
import com.ecom.order.dto.PlaceOrderResponse;
import com.ecom.order.exception.EntityNotFoundException;
import com.ecom.order.exception.OrderAlreadyExistsException;
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

        validateOrderDoesNotExist(orderRequest.getCartId(), customerId);

        CompletableFuture<CustomerDetails> customerFuture = CompletableFuture
                .supplyAsync(() -> customerService.getCustomerDetails(customerId), taskExecutor)
                .orTimeout(10, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture<CartDetails> cartFuture = CompletableFuture
                .supplyAsync(() -> cartService.getCartById(orderRequest.getCartId()), taskExecutor)
                .orTimeout(10, java.util.concurrent.TimeUnit.SECONDS);

        CompletableFuture.allOf(customerFuture, cartFuture).join();

        CustomerDetails customer = customerFuture.join();
        CartDetails cart = cartFuture.join();

        validateCartNotEmpty(cart);
        validateAmountPositive(cart.totalPrice());

        Order order = createAndSaveOrder(orderRequest, customer, cart);

        if (!reserveStockAndHandleFailure(cart, order)) {
            return orderMapper.mapToPlaceOrderResponse(order);
        }

        var paymentDetails = processPayment(customer, order, cart);

        handlePostPaymentActions(order, cart, paymentDetails);

        // TODO publish event OrderPlaced

        log.info("Order placed successfully customerId={}, orderId={}", customerId, order.getId());
        return buildPlaceOrderResponse(order, paymentDetails);
    }

    private void validateOrderDoesNotExist(Long cartId, String customerId) {
        orderRepo.findByCartIdAndCustomerId(cartId, customerId)
                .ifPresent(order -> {
                    throw new OrderAlreadyExistsException(order.getId(), "Order already exists");
                });
    }

    private void validateCartNotEmpty(CartDetails cart) {
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalArgumentException("Cannot place order with empty cart");
        }
    }

    private void validateAmountPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be positive");
        }
    }

    private Order createAndSaveOrder(OrderRequest request, CustomerDetails customer, CartDetails cart) {
        Order order = createOrderFromCart(request, customer, cart);
        return orderRepo.save(order);
    }

    private PlaceOrderResponse.PaymentDetails processPayment(CustomerDetails customer, Order order, CartDetails cart) {
        BigDecimal total = cart.totalPrice();
        CreatePaymentRequest paymentRequest = buildPaymentRequest(customer, order, total);

        try {
            PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
            log.info("Payment created successfully for orderId={}, paymentId={}, amount={}",
                    order.getId(), paymentResponse.paymentId(), total);
            updateOrderWithPayment(order, paymentResponse);
            return buildPaymentDetails(paymentResponse);
        }
        catch (PaymentFailedException e) {
            log.error("Payment failed for orderId={}, amount={}", order.getId(), total, e);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            return null;
        }
        catch (Exception e) {
            log.error("Unexpected error during payment for orderId={}", order.getId(), e);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepo.save(order);
            return null;
        }
    }

    private CreatePaymentRequest buildPaymentRequest(CustomerDetails customer, Order order, BigDecimal total) {
        return CreatePaymentRequest.builder()
                .customerId(customer.id())
                .customerEmail(customer.email())
                .orderId(order.getId().toString())
                .amount(total)
                .build();
    }

    private void updateOrderWithPayment(Order order, PaymentResponse paymentResponse) {
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setPaymentInfo(new PaymentInfo(paymentResponse.paymentId(), null));
        orderRepo.save(order);
    }

    private PlaceOrderResponse.PaymentDetails buildPaymentDetails(PaymentResponse paymentResponse) {
        return PlaceOrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.paymentId())
                .paymentStatus(paymentResponse.status().toString())
                .stripePaymentIntentId(paymentResponse.stripePaymentIntentId())
                .clientSecret(paymentResponse.clientSecret())
                .transactionId(paymentResponse.transactionId())
                .build();
    }

    private void handlePostPaymentActions(Order order, CartDetails cart, PlaceOrderResponse.PaymentDetails paymentDetails) {
        if (paymentDetails == null) {
            // Payment failed - release reserved stock
            CompletableFuture.runAsync(() -> {
                try {
                    inventoryService.releaseReservation(order.getId().toString());
                    log.info("Stock released for failed order {}", order.getId());
                }
                catch (Exception e) {
                    log.error("Failed to release stock for order {}", order.getId(), e);
                }
            }, taskExecutor);
        }
        else {
            // Payment successful - mark cart as completed
            CompletableFuture.runAsync(() -> {
                try {
                    cartService.completeCart(cart.id());
                    log.info("Cart {} marked as completed", cart.id());
                }
                catch (Exception e) {
                    log.error("Failed to complete cart {}", cart.id(), e);
                }
            }, taskExecutor);
        }
    }

    private PlaceOrderResponse buildPlaceOrderResponse(Order order, PlaceOrderResponse.PaymentDetails paymentDetails) {
        return PlaceOrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .paymentDetails(paymentDetails)
                .build();
    }

    private boolean reserveStockAndHandleFailure(CartDetails cart, Order order) {
        Map<Long, Integer> productsWithQuantities = cart.items().stream().collect(toMap(
                CartDetails.CartItem::productId,
                CartDetails.CartItem::quantity
        ));

        try {
            ReserveStockResponse reserveStockResponse = inventoryService.reserveProducts(order.getId().toString(), productsWithQuantities);

            if (!reserveStockResponse.success()) {
                log.error("Stock reservation failed [orderId={}, products={}]: {}",
                        order.getId(), productsWithQuantities.keySet(), reserveStockResponse.message());
                order.setStatus(OrderStatus.RESERVATION_FAILED);
                orderRepo.save(order);
                return false;
            }

            log.info("Stock reserved successfully for orderId={}", order.getId());
            return true;
        }
        catch (Exception e) {
            log.error("Exception during stock reservation for orderId={}", order.getId(), e);
            order.setStatus(OrderStatus.RESERVATION_FAILED);
            orderRepo.save(order);
            return false;
        }
    }

    private Order createOrderFromCart(OrderRequest request, CustomerDetails customer, CartDetails cart) {
        DeliveryInfo deliveryInfo = new DeliveryInfo(
                request.getAddress().street(),
                null,
                request.getAddress().city(),
                null,
                request.getAddress().postalCode(),
                request.getAddress().country()
        );

        Order order = Order.builder()
                .customerId(customer.id())
                .cartId(cart.id())
                .totalAmount(calculateTotalAmount(cart))
                .deliveryInfo(deliveryInfo)
                .build();

        // Create order lines with bidirectional relationship and prices
        List<OrderLine> orderLines = cart.items().stream()
                .map(item -> OrderLine.builder()
                        .quantity(item.quantity())
                        .productId(item.productId())
                        .price(item.price())
                        .order(order)
                        .build())
                .collect(Collectors.toList());

        order.setOrderLines(orderLines);
        return order;
    }

    private BigDecimal calculateTotalAmount(CartDetails cart) {
        return cart.items().stream()
                .reduce(BigDecimal.ZERO, (sum, item) -> sum.add(item.price().multiply(BigDecimal.valueOf(item.quantity()))), BigDecimal::add);
    }

    public Page<OrderResponse> getCustomerOrders(String customerId, int page, int size) {
        return orderRepo.findByCustomerId(customerId, PageRequest.of(page, size))
                .map(orderMapper::fromOrder);
    }

    public Page<OrderResponse> findAllOrders(int page, int size) {
        return this.orderRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date")))
                .map(this.orderMapper::fromOrder);
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

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Order is not in PAYMENT_PENDING status: " + orderId);
        }

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

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            CompletableFuture.runAsync(() -> {
                try {
                    inventoryService.confirmReservation(order.getId());
                    log.info("Stock reservation confirmed for orderId={}", orderId);
                }
                catch (Exception e) {
                    log.error("Failed to confirm stock reservation for orderId={}", orderId, e);
                }
            }, taskExecutor);
        }

        return paymentResponse;
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwner(UUID orderId, String customerId) {
        return orderRepo.findById(orderId)
                .map(order -> customerId.equals(order.getCustomerId()))
                .orElse(true);
    }

    @Transactional
    public void cancelOrder(UUID orderId, String userId) {
        log.info("Cancelling order orderId={}, userId={}", orderId, userId);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.PAID ||
                order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        if (order.getStatus() == OrderStatus.PAYMENT_PENDING
                && order.getPaymentInfo() != null
                && order.getPaymentInfo().getPaymentId() != null) {
            try {
                paymentService.cancelPayment(order.getPaymentInfo().getPaymentId());
                log.info("Payment cancelled for order {}", orderId);
            }
            catch (Exception e) {
                log.error("Failed to cancel payment for order {}", orderId, e);
                throw new IllegalStateException("Failed to cancel payment for order: " + orderId, e);
            }
        }

        if (order.getStatus() == OrderStatus.RESERVED || order.getStatus() == OrderStatus.PAYMENT_PENDING) {
            try {
                inventoryService.releaseReservation(orderId.toString());
                log.info("Stock released for order {}", orderId);
            }
            catch (Exception e) {
                log.error("Failed to release stock for order {}", orderId, e);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

        log.info("Order cancelled successfully orderId={}", orderId);
    }

    public OrderStatsResponse getOrderStats() {
        var stats = new OrderStatsResponse();

        stats.setTotalOrders(orderRepo.count());
        stats.setTotalRevenue(orderRepo.getTotalRevenue());

        return stats;
    }

}
