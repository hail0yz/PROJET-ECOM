package com.ecom.payment.dto;



import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class CreatePaymentRequest {


    @NotNull(message = "Order ID is required")

    private String orderId;


    @NotNull(message = "Amount is required")

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")

    private BigDecimal amount;


    @Email(message = "Invalid email format")

    @NotBlank(message = "Customer email is required")

    private String customerEmail;


    @NotBlank(message = "Customer id is required")

    private String customerId;


    private String description;


    // Constructeurs

    public CreatePaymentRequest() {
    }


    public CreatePaymentRequest(String orderId, BigDecimal amount, String paymentMethod,

                                String customerEmail, String description) {

        this.orderId = orderId;

        this.amount = amount;

        this.customerEmail = customerEmail;

        this.description = description;

    }


    // Getters

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDescription() {
        return description;
    }


    // Setters

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}