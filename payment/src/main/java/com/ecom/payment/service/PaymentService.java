package com.ecom.payment.service;

import com.ecom.payment.dto.*;
import com.ecom.payment.entity.Payment;
import com.ecom.payment.entity.PaymentStatus;
import com.ecom.payment.exception.*;
import com.ecom.payment.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;
    
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());
        
        if (paymentRepository.existsByOrderIdAndStatus(request.getOrderId(), PaymentStatus.COMPLETED)) {
            throw new PaymentAlreadyExistsException("Payment already completed for order ID: " + request.getOrderId());
        }
        
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("Payment amount must be greater than zero");
        }
        
        String transactionId = generateTransactionId();
        String description = request.getDescription() != null ? 
                request.getDescription() : "Order #" + request.getOrderId();
        
        try {
            // Créer un PaymentIntent avec Stripe
            PaymentIntent paymentIntent = stripePaymentService.crPaymentIntent(
                    request.getAmount(),
                    request.getCustomerEmail(),
                    description
            );
            
            // Sauvegarder le paiement dans la base de données
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .status(PaymentStatus.PENDING)
                    .transactionId(transactionId)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .customerEmail(request.getCustomerEmail())
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created - ID: {}, Stripe PaymentIntent: {}", 
                    savedPayment.getPaymentId(), paymentIntent.getId());
            
            return PaymentResponse.builder()
                    .paymentId(savedPayment.getPaymentId())
                    .orderId(savedPayment.getOrderId())
                    .status(savedPayment.getStatus())
                    .transactionId(transactionId)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .amount(savedPayment.getAmount())
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .message("Payment intent created successfully. Use client secret to complete payment.")
                    .build();
            
        } catch (Exception e) {
            log.error("Error while creating payment: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to create payment: " + e.getMessage());
        }
    }
    
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment with ID: {}", request.getPaymentId());
        
        Payment payment = findPaymentById(request.getPaymentId());
        validatePaymentForProcessing(payment);
        
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        
        try {
            // Récupérer le statut du PaymentIntent depuis Stripe
            PaymentIntent paymentIntent = stripePaymentService.captPaymentIntent(
                    payment.getStripePaymentIntentId()
            );
            
            String stripeStatus = paymentIntent.getStatus();
            log.info("Stripe PaymentIntent status: {}", stripeStatus);
            
            // Mapper le statut Stripe au statut local
            switch (stripeStatus) {
                case "succeeded":
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setStripeChargeId(paymentIntent.getLatestCharge());
                    payment.setFailureReason(null);
                    paymentRepository.save(payment);
                    log.info("Payment completed successfully - ID: {}", payment.getPaymentId());
                    return buildPaymentResponse(payment, "Payment completed successfully");
                    
                case "requires_action":
                case "requires_payment_method":
                    payment.setStatus(PaymentStatus.REQUIRES_ACTION);
                    paymentRepository.save(payment);
                    return PaymentResponse.builder()
                            .paymentId(payment.getPaymentId())
                            .orderId(payment.getOrderId())
                            .status(PaymentStatus.REQUIRES_ACTION)
                            .transactionId(payment.getTransactionId())
                            .stripePaymentIntentId(payment.getStripePaymentIntentId())
                            .clientSecret(paymentIntent.getClientSecret())
                            .amount(payment.getAmount())
                            .paymentMethod(payment.getPaymentMethod())
                            .message("Payment requires additional action from customer")
                            .build();
                    
                case "canceled":
                    payment.setStatus(PaymentStatus.CANCELLED);
                    payment.setFailureReason("Payment cancelled");
                    paymentRepository.save(payment);
                    return buildPaymentResponse(payment, "Payment was cancelled");
                    
                case "processing":
                    payment.setStatus(PaymentStatus.PROCESSING);
                    paymentRepository.save(payment);
                    return buildPaymentResponse(payment, "Payment is being processed");
                    
                default:
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Unknown Stripe status: " + stripeStatus);
                    paymentRepository.save(payment);
                    return buildPaymentResponse(payment, "Payment failed");
            }
            
        } catch (Exception e) {
            log.error("Error while processing payment: {}", e.getMessage(), e);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Error: " + e.getMessage());
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Failed to process payment: " + e.getMessage());
        }
    }
    
    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        log.info("Processing refund for payment ID: {}", request.getPaymentId());
        
        Payment payment = findPaymentById(request.getPaymentId());
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Only completed payments can be refunded");
        }
        
        if (payment.getStripeChargeId() == null) {
            throw new RefundFailedException("No Stripe charge ID found for this payment");
        }
        
        BigDecimal refundAmount = request.getRefundAmount() != null ? 
                request.getRefundAmount() : payment.getAmount();
        
        validateRefundAmount(refundAmount, payment.getAmount());
        
        try {
            // Créer un remboursement avec Stripe
            Refund refund = stripePaymentService.createRefund(
                    payment.getStripeChargeId(),
                    refundAmount,
                    request.getReason()
            );
            
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setFailureReason("Refunded: " + request.getReason());
            paymentRepository.save(payment);
            
            log.info("Payment refunded successfully - ID: {}, Stripe Refund: {}", 
                    payment.getPaymentId(), refund.getId());
            
            return buildPaymentResponse(payment, "Payment refunded successfully");
            
        } catch (StripeException e) {
            log.error("Stripe error while refunding payment: {}", e.getMessage(), e);
            throw new RefundFailedException("Failed to refund payment with Stripe: " + e.getMessage());
        }
    }
    
    @Transactional
    public void cancelPayment(Integer paymentId) {
        log.info("Cancelling payment with ID: {}", paymentId);
        
        Payment payment = findPaymentById(paymentId);
        validatePaymentForCancellation(payment);
        
        try {
            // Annuler le PaymentIntent avec Stripe
            if (payment.getStripePaymentIntentId() != null) {
                stripePaymentService.cancelPaymentIntent(payment.getStripePaymentIntentId());
            }
            
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("Cancelled by user");
            paymentRepository.save(payment);
            
            log.info("Payment cancelled successfully - ID: {}", paymentId);
            
        } catch (StripeException e) {
            log.error("Stripe error while cancelling payment: {}", e.getMessage(), e);
            // Continuer l'annulation même si Stripe échoue
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setFailureReason("Cancelled by user (Stripe cancellation failed)");
            paymentRepository.save(payment);
        }
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Integer paymentId) {
        log.info("Fetching payment with ID: {}", paymentId);
        Payment payment = findPaymentById(paymentId);
        return mapToDTO(payment);
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOrderId(Integer orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order ID: " + orderId));
        return mapToDTO(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPaymentsByOrderId(Integer orderId) {
        log.info("Fetching all payments for order ID: {}", orderId);
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        log.info("Fetching payment with transaction ID: {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with transaction ID: " + transactionId));
        return mapToDTO(payment);
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        log.info("Fetching all payments");
        return paymentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status) {
        log.info("Fetching payments with status: {}", status);
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByPaymentMethod(String paymentMethod) {
        log.info("Fetching payments with payment method: {}", paymentMethod);
        return paymentRepository.findByPaymentMethod(paymentMethod).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByCustomerEmail(String email) {
        log.info("Fetching payments for customer email: {}", email);
        return paymentRepository.findByCustomerEmail(email).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }@Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByDateRange(LocalDateTime start, LocalDateTime end) {
        log.info("Fetching payments between {} and {}", start, end);
        return paymentRepository.findByDateCreationBetween(start, end).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByStatus(PaymentStatus status) {
        log.info("Calculating total amount for status: {}", status);
        BigDecimal total = paymentRepository.sumAmountByStatus(status);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public Long getPaymentCountByStatus(PaymentStatus status) {
        log.info("Counting payments with status: {}", status);
        return paymentRepository.countByStatus(status);
    }
    
    @Transactional
    public PaymentResponse syncPaymentWithStripe(Integer paymentId) {
        log.info("Syncing payment {} with Stripe", paymentId);
        
        Payment payment = findPaymentById(paymentId);
        
        if (payment.getStripePaymentIntentId() == null) {
            throw new PaymentProcessingException("No Stripe PaymentIntent ID found for this payment");
        }
        
        try {
            PaymentIntent paymentIntent = stripePaymentService.retrievePaymentIntent(
                    payment.getStripePaymentIntentId()
            );
            
            String stripeStatus = paymentIntent.getStatus();
            log.info("Synced status from Stripe: {}", stripeStatus);
            
            // Mettre à jour le statut en fonction de Stripe
            switch (stripeStatus) {
                case "succeeded":
                    payment.setStatus(PaymentStatus.COMPLETED);
                    payment.setStripeChargeId(paymentIntent.getLatestCharge());
                    break;
                case "requires_action":
                case "requires_payment_method":
                    payment.setStatus(PaymentStatus.REQUIRES_ACTION);
                    break;
                case "canceled":
                    payment.setStatus(PaymentStatus.CANCELLED);
                    break;
                case "processing":
                    payment.setStatus(PaymentStatus.PROCESSING);
                    break;
                default:
                    payment.setStatus(PaymentStatus.PENDING);
            }
            
            paymentRepository.save(payment);
            return buildPaymentResponse(payment, "Payment synced with Stripe successfully");
            
        } catch (Exception e) {
            log.error("Error syncing payment with Stripe: {}", e.getMessage(), e);
            throw new PaymentProcessingException("Failed to sync payment with Stripe: " + e.getMessage());
        }
    }
    
    // Helper methods
    private Payment findPaymentById(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: " + paymentId));
    }
    
    private void validatePaymentForProcessing(Payment payment) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentAlreadyProcessedException("Payment already completed");
        }
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new PaymentCancelledException("Cannot process a cancelled payment");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStatusException("Cannot process a refunded payment");
        }
    }
    
    private void validatePaymentForCancellation(Payment payment) {
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStatusException("Cannot cancel a completed payment. Use refund instead.");
        }
        if (payment.getStatus() == PaymentStatus.PROCESSING) {
            throw new InvalidPaymentStatusException("Cannot cancel a payment that is being processed");
        }
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new InvalidPaymentStatusException("Payment is already cancelled");
        }
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStatusException("Cannot cancel a refunded payment");
        }
    }
    
    private void validateRefundAmount(BigDecimal refundAmount, BigDecimal paymentAmount) {
        if (refundAmount.compareTo(paymentAmount) > 0) {
            throw new InvalidRefundAmountException("Refund amount cannot exceed original payment amount");
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundAmountException("Refund amount must be greater than zero");
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private PaymentResponse buildPaymentResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .message(message)
                .failureReason(payment.getFailureReason())
                .build();
    }
    
    private PaymentDTO mapToDTO(Payment payment) {
        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .dateCreation(payment.getDateCreation())
                .transactionId(payment.getTransactionId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .stripeChargeId(payment.getStripeChargeId())
                .failureReason(payment.getFailureReason())
                .customerEmail(payment.getCustomerEmail())
                .build();
    }
}