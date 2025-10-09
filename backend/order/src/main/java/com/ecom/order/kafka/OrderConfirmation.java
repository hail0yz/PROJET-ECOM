package com.ecom.order.kafka;


import com.ecom.order.customer.CustomerResponse;
import com.ecom.order.model.PaymentMethod;
import com.ecom.order.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> purchasedProducts
) {


}
