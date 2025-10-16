package com.ecom.payment.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="payment_id")
    private Integer paymentId;

    @Column(name="order_id",nullable=false)
    private Integer orderId;

    @Column(name="amount",nullable=false)
    private BigDecimal amount;

    @Column(name="payment_method")
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name="creation_date")
    private LocalDateTime creationDate;

    @Column(name="user_email")
    private String userEmail;

    //Might need to add currency
}