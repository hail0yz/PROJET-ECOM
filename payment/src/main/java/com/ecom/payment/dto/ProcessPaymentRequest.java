package com.ecom.payment.dto;

import jakarta.validation.constraints.NotNull;
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
public class ProcessPaymentRequest {
    
    @NotNull(message = "Payment ID is required")
    private Integer paymentId;
    
    // Code de vérification pour la sécurité
    private String verificationCode;
    
    // Détails additionnels si nécessaire
    private PaymentDetailsDTO paymentDetails;
}