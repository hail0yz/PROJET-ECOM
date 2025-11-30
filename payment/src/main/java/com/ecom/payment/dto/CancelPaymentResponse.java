package com.ecom.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelPaymentResponse {

    private boolean canceled;

    private String message;

}
