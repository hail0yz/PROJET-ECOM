package com.ecom.order.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record PaymentInfo(
        @Enumerated(EnumType.STRING) PaymentMethod paymentMethod
) {
}
