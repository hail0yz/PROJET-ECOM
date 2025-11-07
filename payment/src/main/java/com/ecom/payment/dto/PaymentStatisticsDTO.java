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
public class PaymentStatisticsDTO {
    private Long totalPayments;
    private Long completedPayments;
    private Long failedPayments;
    private Long pendingPayments;
    private Long refundedPayments;
    private BigDecimal totalAmount;
    private BigDecimal totalCompletedAmount;
    private BigDecimal totalRefundedAmount;
    private Double successRate;
}