package com.ecom.order.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private String orderId;

    private BigDecimal amount;

    private String paymentMethod;

    private String customerEmail;

    private String customerId;

    private String description;

}