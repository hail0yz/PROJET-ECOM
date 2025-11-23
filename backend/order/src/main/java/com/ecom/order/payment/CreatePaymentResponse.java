package com.ecom.order.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentResponse {

    private Long paymentId;

    private String stripePaymentIntentId;

    private String clientSecret;

}
