package com.ecom.payment.repository;

import com.ecom.payment.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRepository {
    Optional<Payment> findByOrderId(Integer orderId);
}