package com.ecom.order.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record PaymentInfo(Long paymentId, String paymentMethod) {

    public PaymentInfo withPaymentId(Long paymentId) {
        return new PaymentInfo(paymentId, this.paymentMethod);
    }

}
