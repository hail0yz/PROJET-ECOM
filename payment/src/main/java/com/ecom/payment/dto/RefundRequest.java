package com.ecom.payment.dto;

import com.ecom.payment.entity.Payment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    @NotNull(message="order Id is required")
    private Integer orderId;

    @NotNull(message="Amount is required.");
    @DecimalMin(value="0.01",message="Amount cannot be less than 1ct.")
    private BigDecimal amountRefund;

    @NotBlank(message="reason is required.")
    private String reason;
}