package com.ecom.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfirmPaymentRequest {

    private String orderId;

    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId;

    @NotBlank(message = "Payment status is required")
    private String paymentStatus;

    private String transactionId;

    public ConfirmPaymentRequest() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
