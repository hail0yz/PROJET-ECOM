package com.ecom.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.payment.entity.Payment;
import com.ecom.payment.entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Trouver un paiement par ID de commande
    Optional<Payment> findByOrderId(Integer orderId);
    
    // Trouver tous les paiements d'une commande
    List<Payment> findAllByOrderId(Integer orderId);
    
    // Trouver par ID de transaction
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Trouver par Stripe PaymentIntent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    // Trouver tous les paiements avec un certain statut
    List<Payment> findByStatus(PaymentStatus status);
    
    // Trouver les paiements par méthode de paiement
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    // Trouver les paiements dans une plage de dates
    List<Payment> findByDateCreationBetween(LocalDateTime start, LocalDateTime end);
    
    // Calculer le total des paiements par statut
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
    
    // Compter les paiements par statut
    Long countByStatus(PaymentStatus status);
    
    // Trouver les paiements en attente depuis plus de X minutes
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.dateCreation < :threshold")
    List<Payment> findStalePendingPayments(@Param("threshold") LocalDateTime threshold);
    
    // Trouver les paiements par email client
    List<Payment> findByCustomerEmail(String customerEmail);
    
    // Vérifier si un paiement existe pour une commande avec un statut spécifique
    boolean existsByOrderIdAndStatus(Integer orderId, PaymentStatus status);
    
    // Alternative si la méthode au-dessus ne fonctionne pas
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.orderId = :orderId AND p.status = :status")
    boolean checkIfPaymentExistsByOrderIdAndStatus(@Param("orderId") Integer orderId, @Param("status") PaymentStatus status);
}