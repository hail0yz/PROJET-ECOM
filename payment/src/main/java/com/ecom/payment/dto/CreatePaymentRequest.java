package com.ecom.payment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Integer orderId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Customer email is required")
    private String customerEmail;
    
    private String description;
    
    // Constructeurs
    public CreatePaymentRequest() {
    }
    
    public CreatePaymentRequest(Integer orderId, BigDecimal amount, String paymentMethod, 
                               String customerEmail, String description) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.customerEmail = customerEmail;
        this.description = description;
    }
    
    // Getters
    public Integer getOrderId() {
        return orderId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Setters
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Builder pattern (optionnel)
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Integer orderId;
        private BigDecimal amount;
        private String paymentMethod;
        private String customerEmail;
        private String description;
        
        public Builder orderId(Integer orderId) {
            this.orderId = orderId;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(orderId, amount, paymentMethod, customerEmail, description);
        }
    }
}