package com.ecom.payment.dto;
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
public class PaymentDetailsDTO {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;
    private String billingAddress;
    private String postalCode;
}