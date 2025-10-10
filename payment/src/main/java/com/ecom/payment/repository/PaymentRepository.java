package com.ecom.payment.repository;

import com.ecom.payment.entity.Payment;
import com.ecom.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository

public class PaymentRepository {
    Optional<Payment> findByOrderId(Integer orderId);
    List<Payment> findAllByOrderId(Integer orderId);
    List<Payment> findByStatus(PaymentStatus paymentStatus);
    List<Payment> findByPaymentMethod(String paymentMethod);
    List<Payment> findByDate(LocalDateTime creationDate, LocalDateTime processDate);
    
    @Query( "a completer")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
    Long countByStatus(Payment status);

    List<Payment> findByUserEmail (String userEmail);
}