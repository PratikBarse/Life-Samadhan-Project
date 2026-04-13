package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.model.Payment;
import com.lifesamadhan.api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order/{assignmentId}")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(paymentService.createRazorpayOrderDetail(assignmentId));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody(required = false) Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Payment verification data is missing"));
        }
        paymentService.verifyRazorpayPayment(data);
        return ResponseEntity.ok(Map.of("message", "Payment verified successfully"));
    }

    @GetMapping("/earnings/{providerId}")
    public ResponseEntity<Map<String, Object>> getEarnings(@PathVariable Long providerId) {
        return ResponseEntity.ok(paymentService.getProviderEarnings(providerId));
    }

    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody Payment payment) {
        Payment savedPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(savedPayment);
    }

    @PutMapping("/{id}/success")
    public ResponseEntity<?> markPaymentSuccess(@PathVariable Long id) {
        try {
            Payment payment = paymentService.markPaymentSuccess(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/failed")
    public ResponseEntity<?> markPaymentFailed(@PathVariable Long id) {
        try {
            Payment payment = paymentService.markPaymentFailed(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}