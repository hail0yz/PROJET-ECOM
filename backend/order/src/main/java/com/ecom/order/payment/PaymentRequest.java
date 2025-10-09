package com.ecom.order.payment;

import com.ecom.order.customer.CustomerResponse;
import com.ecom.order.model.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        String reference,
        UUID orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        CustomerResponse customer
) {
}
