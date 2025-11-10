package com.ecom.payment.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.payment.dto.CreatePaymentRequest;
import com.ecom.payment.dto.PaymentDTO;
import com.ecom.payment.dto.PaymentResponse;
import com.ecom.payment.dto.ProcessPaymentRequest;
import com.ecom.payment.dto.RefundRequest;
import com.ecom.payment.entity.PaymentStatus;
import com.ecom.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<Map<String, String>> cancelPayment(@PathVariable Integer paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(Map.of("message", "Payment cancelled successfully"));
    }
    
    @GetMapping("/{paymentId}/sync")
    public ResponseEntity<PaymentResponse> syncPaymentWithStripe(@PathVariable Integer paymentId) {
        PaymentResponse response = paymentService.syncPaymentWithStripe(paymentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Integer paymentId) {
        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDTO> getPaymentByOrderId(@PathVariable Integer orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping("/order/{orderId}/all")
    public ResponseEntity<List<PaymentDTO>> getAllPaymentsByOrderId(@PathVariable Integer orderId) {
        List<PaymentDTO> payments = paymentService.getAllPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentDTO> getPaymentByTransactionId(@PathVariable String transactionId) {
        PaymentDTO payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(payment);
    }
    
    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByPaymentMethod(@PathVariable String paymentMethod) {
        List<PaymentDTO> payments = paymentService.getPaymentsByPaymentMethod(paymentMethod);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByCustomerEmail(@PathVariable String email) {
        List<PaymentDTO> payments = paymentService.getPaymentsByCustomerEmail(email);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<PaymentDTO> payments = paymentService.getPaymentsByDateRange(start, end);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/total/{status}")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmountByStatus(@PathVariable PaymentStatus status) {
        BigDecimal total = paymentService.getTotalAmountByStatus(status);
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }
    
    @GetMapping("/count/{status}")
    public ResponseEntity<Map<String, Long>> getPaymentCountByStatus(@PathVariable PaymentStatus status) {
        Long count = paymentService.getPaymentCountByStatus(status);
        return ResponseEntity.ok(Map.of("count", count));
    }
}