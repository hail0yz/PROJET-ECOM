package com.ecom.order.dto;

import java.util.UUID;

import com.ecom.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {

    private UUID orderId;

    private PaymentDetails paymentDetails;

    private OrderStatus orderStatus;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentDetails {
        private Long paymentId;
        private String paymentStatus;
        private String transactionId;
        private String paymentMethod;
        private String stripePaymentIntentId;
        private String clientSecret;
    }

}
