package com.ecom.order.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record DeliveryInfo(
        String address1,
        String address2,
        String city,
        String state,
        String postalCode,
        String country
) {
}