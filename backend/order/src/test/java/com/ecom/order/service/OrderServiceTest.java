package com.ecom.order.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import com.ecom.order.cart.CartDetails;
import com.ecom.order.customer.CustomerDetails;
import com.ecom.order.dto.OrderRequest;
import com.ecom.order.dto.PlaceOrderResponse;
import com.ecom.order.exception.EntityNotFoundException;
import com.ecom.order.mapper.OrderMapper;
import com.ecom.order.model.Order;
import com.ecom.order.model.OrderStatus;
import com.ecom.order.model.PaymentInfo;
import com.ecom.order.payment.PaymentResponse;
import com.ecom.order.product.ReserveStockResponse;
import com.ecom.order.repository.OrderRepo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepo orderRepo;

    @Mock
    CustomerService customerService;

    @Mock
    OrderMapper orderMapper;

    @Mock
    CartService cartService;

    @Mock
    InventoryService inventoryService;

    @Mock
    PaymentService paymentService;

    Executor syncExecutor = Runnable::run;

    OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepo, customerService, orderMapper, cartService, inventoryService, paymentService, syncExecutor);
    }

    @Test
    void placeOrder_reserveStockFails_returnsFailed() {
        OrderRequest.PaymentDetails pay = new OrderRequest.PaymentDetails("card");
        OrderRequest.Address addr = new OrderRequest.Address("street", "city", "zip", "country");
        OrderRequest req = OrderRequest.builder()
                .cartId(1L)
                .address(addr)
                .build();

        CustomerDetails customer = new CustomerDetails("cust-1", "ext-1", "Fn", "Ln", "mail@example.com", null, null, true, null);
        when(customerService.getCustomerDetails(anyString())).thenReturn(customer);

        CartDetails.CartItem item = new CartDetails.CartItem(100L, 1, new BigDecimal("10.00"));
        CartDetails cart = new CartDetails(1L, "cust-1", List.of(item), LocalDateTime.now(), LocalDateTime.now(), new BigDecimal("10.00"));
        when(cartService.getCartById(anyLong())).thenReturn(cart);

        when(orderRepo.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        when(inventoryService.reserveProducts(anyString(), anyMap()))
                .thenReturn(new ReserveStockResponse(null, false, "no stock"));

        when(orderMapper.mapToPlaceOrderResponse(any()))
                .thenAnswer(invocation -> {
                    Order o = invocation.getArgument(0);
                    return PlaceOrderResponse.builder()
                            .orderId(o.getId())
                            .orderStatus(o.getStatus())
                            .paymentDetails(null)
                            .build();
                });

        PlaceOrderResponse response = orderService.placeOrder(req, "cust-1");

        assertNotNull(response);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.RESERVATION_FAILED);
    }

    @Test
    void placeOrder_paymentSuccess_callsCompleteCart_and_returnsPaymentDetails() {
        OrderRequest.PaymentDetails pay = new OrderRequest.PaymentDetails("card");
        OrderRequest.Address addr = new OrderRequest.Address("street", "city", "zip", "country");
        OrderRequest req = OrderRequest.builder().cartId(1L).address(addr).build();

        CustomerDetails customer = new CustomerDetails("cust-1", "ext-1", "Fn", "Ln", "mail@example.com", null, null, true, null);
        when(customerService.getCustomerDetails(anyString())).thenReturn(customer);

        CartDetails.CartItem item = new CartDetails.CartItem(100L, 1, new BigDecimal("10.00"));
        CartDetails cart = new CartDetails(1L, "cust-1", List.of(item), LocalDateTime.now(), LocalDateTime.now(), new BigDecimal("10.00"));
        when(cartService.getCartById(anyLong())).thenReturn(cart);

        when(orderRepo.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        when(inventoryService.reserveProducts(anyString(), anyMap())).thenReturn(new ReserveStockResponse(null, true, null));

        PaymentResponse paymentResponse = new PaymentResponse(11L, "ord", PaymentResponse.PaymentStatus.PENDING, null, null, null, new BigDecimal("10.00"), "card", null, null);
        when(paymentService.createPayment(any())).thenReturn(paymentResponse);

        PlaceOrderResponse resp = orderService.placeOrder(req, "cust-1");

        assertThat(resp).isNotNull();
        assertThat(resp.getPaymentDetails()).isNotNull();

        verify(cartService).completeCart(1L);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @Test
    void placeOrder_paymentFails_callsReleaseReservation_and_returnsPaymentFailed() {
        OrderRequest.PaymentDetails pay = new OrderRequest.PaymentDetails("card");
        OrderRequest.Address addr = new OrderRequest.Address("street", "city", "zip", "country");
        OrderRequest req = OrderRequest.builder().cartId(1L).address(addr).build();

        CustomerDetails customer = new CustomerDetails("cust-1", "ext-1", "Fn", "Ln", "mail@example.com", null, null, true, null);
        when(customerService.getCustomerDetails(anyString())).thenReturn(customer);

        CartDetails.CartItem item = new CartDetails.CartItem(100L, 1, new BigDecimal("10.00"));
        CartDetails cart = new CartDetails(1L, "cust-1", List.of(item), LocalDateTime.now(), LocalDateTime.now(), new BigDecimal("10.00"));
        when(cartService.getCartById(anyLong())).thenReturn(cart);

        when(orderRepo.save(any())).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        when(inventoryService.reserveProducts(anyString(), anyMap())).thenReturn(new ReserveStockResponse(null, true, null));

        when(paymentService.createPayment(any())).thenThrow(new com.ecom.order.payment.PaymentFailedException());

        PlaceOrderResponse resp = orderService.placeOrder(req, "cust-1");

        assertThat(resp).isNotNull();
        verify(inventoryService).releaseReservation(anyString());
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    void confirmOrderPayment_completed_setsConfirmed_and_callsInventoryConfirm() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).paymentInfo(null).build();
        order.setPaymentInfo(new PaymentInfo(123L, "card"));
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepo.findById(id)).thenReturn(Optional.of(order));

        PaymentResponse resp = new PaymentResponse(1L, id.toString(), PaymentResponse.PaymentStatus.COMPLETED, null, null, null, BigDecimal.TEN, "card", null, null);
        when(paymentService.syncPayment(123L)).thenReturn(resp);

        var returned = orderService.confirmOrderPayment(id);

        assertThat(returned).isEqualTo(resp);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepo).save(order);
        verify(inventoryService).confirmReservation(id);
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(orderRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> orderService.findById(id));
    }

    @Test
    void isOrderOwner_true_whenMatches_and_true_whenNotFound() {
        UUID id = UUID.randomUUID();
        Order order = Order.builder().id(id).customerId("c1").build();
        when(orderRepo.findById(id)).thenReturn(Optional.of(order));

        assertThat(orderService.isOrderOwner(id, "c1")).isTrue();

        UUID missing = UUID.randomUUID();
        when(orderRepo.findById(missing)).thenReturn(Optional.empty());
        assertThat(orderService.isOrderOwner(missing, "x")).isTrue();
    }

}
