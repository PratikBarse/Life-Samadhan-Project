package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByAssignmentId(Long assignmentId);

    List<Payment> findByPaymentStatus(String paymentStatus);

    java.util.Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM Payment p JOIN p.assignment a WHERE a.providerId = :providerId AND p.paymentStatus = 'COMPLETED'")
    java.math.BigDecimal calculateTotalEarningsForProvider(Long providerId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(p) FROM Payment p JOIN p.assignment a WHERE a.providerId = :providerId AND p.paymentStatus = 'COMPLETED'")
    Long countCompletedPaymentsForProvider(Long providerId);
}