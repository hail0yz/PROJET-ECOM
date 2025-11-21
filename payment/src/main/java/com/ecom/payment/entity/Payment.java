package com.ecom.payment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "payment_id")

    private Integer paymentId;

    @Column(name = "order_id", nullable = false)

    private String orderId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)

    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)

    private String paymentMethod;

    @Enumerated(EnumType.STRING)

    @Column(name = "status", length = 50)

    @Builder.Default

    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp

    @Column(name = "date_creation", nullable = false, updatable = false)

    private LocalDateTime dateCreation;

    @Column(name = "transaction_id", unique = true)

    private String transactionId;

    @Column(name = "stripe_payment_intent_id", unique = true)

    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")

    private String stripeChargeId;

    @Column(name = "failure_reason")

    private String failureReason;

    @Column(name = "customer_email")

    private String customerEmail;

    @Column(name = "customer_id")

    private String customerId;
}
