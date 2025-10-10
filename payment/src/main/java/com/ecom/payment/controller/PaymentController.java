package com.example.payment.controller;

import com.example.payment.dto.*;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
}