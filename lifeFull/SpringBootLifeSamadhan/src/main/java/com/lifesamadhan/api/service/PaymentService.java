package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.Payment;
import com.lifesamadhan.api.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;
import org.json.JSONObject;
import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final com.lifesamadhan.api.repository.ServiceAssignmentRepository serviceAssignmentRepository;
    private final com.lifesamadhan.api.repository.RatingRepository ratingRepository;
    private final com.lifesamadhan.api.repository.ServiceRequestRepository serviceRequestRepository;

    @Value("${razorpay.key_id:rzp_test_YOUR_KEY_ID}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret:YOUR_KEY_SECRET}")
    private String razorpayKeySecret;

    public Payment createPayment(Payment payment) {
        payment.setPaymentStatus("PENDING");
        return paymentRepository.save(payment);
    }

    public Payment markPaymentSuccess(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus("COMPLETED");
        return paymentRepository.save(payment);
    }

    public Payment markPaymentFailed(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus("FAILED");
        return paymentRepository.save(payment);
    }

    public Map<String, Object> createRazorpayOrderDetail(Long assignmentId) {
        try {
            com.lifesamadhan.api.model.ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            Double amountVal = 500.0;
            if (assignment.getRequest() != null && assignment.getRequest().getAmount() != null) {
                amountVal = assignment.getRequest().getAmount();
            } else if (assignment.getProvider() != null && assignment.getProvider().getHourlyRate() != null) {
                amountVal = assignment.getProvider().getHourlyRate();
            }

            BigDecimal amount = BigDecimal.valueOf(amountVal);
            String orderId;

            if (razorpayKeyId == null || razorpayKeyId.contains("YOUR_KEY") || razorpayKeyId.isEmpty()) {
                orderId = "order_mock_" + System.currentTimeMillis();
            } else {
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue());
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "txn_" + assignmentId + "_" + System.currentTimeMillis());

                Order order = razorpay.orders.create(orderRequest);
                orderId = order.get("id");
            }

            
            Payment payment = Payment.builder()
                    .assignmentId(assignmentId)
                    .amount(amount)
                    .paymentStatus("PENDING")
                    .razorpayOrderId(orderId)
                    .build();
            paymentRepository.save(payment);

            return Map.of(
                    "orderId", orderId,
                    "amount", amountVal,
                    "keyId", razorpayKeyId);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyRazorpayPayment(Map<String, String> data) {
        String orderId = data.get("razorpayOrderId");
        String paymentId = data.get("razorpayPaymentId");

        if (orderId == null || paymentId == null) {
            throw new RuntimeException("Invalid payment data");
        }

        
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for order: " + orderId));

        
        payment.setPaymentStatus("COMPLETED");
        paymentRepository.save(payment);

        
        com.lifesamadhan.api.model.ServiceAssignment sa = serviceAssignmentRepository
                .findById(payment.getAssignmentId())
                .orElse(null);
        if (sa != null && sa.getRequest() != null) {
            com.lifesamadhan.api.model.ServiceRequest sr = sa.getRequest();
            sr.setPaymentStatus("PAID");
            serviceRequestRepository.save(sr);
        }
    }

    public Map<String, Object> getProviderEarnings(Long providerId) {
        BigDecimal total = paymentRepository.calculateTotalEarningsForProvider(providerId);
        Long count = paymentRepository.countCompletedPaymentsForProvider(providerId);
        Double avgRating = ratingRepository.getAverageRatingForProvider(providerId);

        return Map.of(
                "totalEarnings", total != null ? total : BigDecimal.ZERO,
                "completedJobs", count != null ? count : 0L,
                "averageRating", avgRating != null ? String.format("%.1f", avgRating) : "0.0");
    }
}