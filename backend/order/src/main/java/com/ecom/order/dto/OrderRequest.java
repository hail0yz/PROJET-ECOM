package com.ecom.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderRequest {

    @NotNull
    private Long cartId;

    private String reference;

    @NotNull
    @Valid
    private Address address;

    @NotNull
    @Valid
    private PaymentDetails paymentDetails;

    public record Address(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String postalCode,
            @NotBlank String country
    ) {
    }

    public record PaymentDetails(
            @NotBlank String paymentMethod
    ) {}

}
