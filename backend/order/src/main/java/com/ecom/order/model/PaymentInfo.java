package com.ecom.order.model;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {
    private Long paymentId;

    private String paymentMethod; // NOTE: not used anymore

}
