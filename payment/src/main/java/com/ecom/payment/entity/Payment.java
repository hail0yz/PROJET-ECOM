package com.ecom.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @CreationTimeStamp
    @Column(name="creation_date")
    private LocalDateTime creationDate;

    @Column(name="user_email")
    private String userEmail;

    //Might need to add currency
}