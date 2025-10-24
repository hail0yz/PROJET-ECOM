package com.ecom.order.dto;

import com.ecom.order.model.PaymentMethod;
import com.ecom.order.product.PurchaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderRequest {

    private String reference;

    @Positive(message = "amount should be positive")
    private BigDecimal ammount;

    @NotNull(message = "payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Customer should be present")
    @NotEmpty(message = "Customer should be present")
    @NotBlank(message = "Customer should be present")
    String customerId;

    @NotEmpty(message = "shoud be atleast one present product")
    private List<PurchaseRequest> products;
}
