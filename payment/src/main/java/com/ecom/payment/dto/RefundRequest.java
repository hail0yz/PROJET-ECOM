package com.ecom.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RefundRequest {
    @NotNull(message="order Id is required")
    private Integer orderId;

    @NotNull(message="Amount is required.")
    @DecimalMin(value="0.01",message="Amount cannot be less than 1ct.")
    private BigDecimal amountRefund;

    @NotBlank(message="reason is required.")
    private String reason;
    @NotNull(message = "Payment ID is required")

    private Integer paymentId;

    

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

}