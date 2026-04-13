package com.lifesamadhan.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "CustomerId is required")
    @Min(value = 1, message = "CustomerId must be a valid value")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotNull(message = "ServiceId is required")
    @Min(value = 1, message = "ServiceId must be a valid value")
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "provider_id")
    private Long providerId;

    @Pattern(regexp = "^(PENDING|ASSIGNED|IN_PROGRESS|STARTED|COMPLETED|CANCELLED)$", message = "Status must be PENDING, ASSIGNED, IN_PROGRESS, STARTED, COMPLETED or CANCELLED")
    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "service_address")
    private String serviceAddress;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_status")
    @Builder.Default
    private String paymentStatus = "PENDING";

    @Column(name = "paid_amount")
    @Builder.Default
    private Double paidAmount = 0.0;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private Location location;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<ServiceAssignment> assignments;
}