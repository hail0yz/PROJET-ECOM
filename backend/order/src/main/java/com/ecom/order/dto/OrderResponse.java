package com.ecom.order.dto;

import com.ecom.order.model.OrderStatus;
import com.ecom.order.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private Long cartId;
    private String reference;
    private String customerId;
    private PaymentMethod payment_method;
    private BigDecimal amount;
    private List<OrderLineResponse> lines;
    private OrderStatus status;
    private DeliveryInfo deliveryInfo;

    public record DeliveryInfo(
            String address1,
            String address2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
    }

}
