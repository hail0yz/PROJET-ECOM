package com.ecom.payment.entity;

public enum  PaymentMethod {
    CREDIT("Credit card"),
    DEBIT("Debit Card"),
    PAYPAL("Paypal");

    private final String displayName;

    PaymentMethod(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}