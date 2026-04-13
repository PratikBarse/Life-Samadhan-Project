package com.lifesamadhan.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAssignmentResponseDTO {

    private Long id;
    private Long requestId;
    private Long providerId;
    private String status;
    private String otp;
    private String serviceName;
    private String providerName;
    private Double hourlyRate;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private ServiceRequestAssignmentDTO request;
    private String paymentStatus;
    private Double paidAmount;
    private Integer ratingStars; 
    private String ratingFeedback;
    private LocalDateTime completedAt;
}
