package com.ecom.payment.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse{
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    //private PaymentStatus status;
    private String failureReason;
    private String message;
}