package com.ecom.order.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Validated
public class PurchaseRequest {

    @NotNull(message = "product is mandatory")
    private UUID productId;

    @Positive(message = "quantity must be positive")
    private BigDecimal quantity;
}
