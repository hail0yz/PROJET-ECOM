package com.ecom.payment.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    @NotNull (message="order ID is required.")
    private Integer orderID;

    @NotNull(message="Amount is required.")
    @DecimalMin(value="0.01",message="Amount cannot be less than 1ct.")
    private BigDecimal amount;

    @NotBlank(message="Payment method is required.")
    private String paymentMethod;

    @Email(message="Email is not valid.")
    private String userEmail;

}