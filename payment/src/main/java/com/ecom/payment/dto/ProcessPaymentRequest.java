package com.ecom.payment.dto;

import com.ecom.payment.entity.Payment;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequest {
    @NotNull(message= "order id is required.")
    private Integer paymentId;
}