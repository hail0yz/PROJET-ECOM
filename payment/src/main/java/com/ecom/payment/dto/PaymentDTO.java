package com.ecom.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecom.payment.entity.PaymentStatus;

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
public class PaymentDTO {
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime dateCreation;
    private String transactionId;
    private String failureReason;
    private String customerEmail;
    private String currency;
    private String stripePaymentIntentId;
    private String stripeChargeId;

}