package com.ecom.order.dto;

import com.ecom.order.model.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class OrderResponse {
    private String orderId;
    private String reference;
    private String customerId;
    private PaymentMethod payment_method;
    private BigDecimal amount;
}
