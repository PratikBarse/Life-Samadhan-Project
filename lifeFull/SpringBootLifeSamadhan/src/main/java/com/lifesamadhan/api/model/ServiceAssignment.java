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
@Table(name = "service_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "RequestId is required")
    @Min(value = 1, message = "RequestId must be a valid value")
    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @NotNull(message = "ProviderId is required")
    @Min(value = 1, message = "ProviderId must be a valid value")
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Pattern(regexp = "^(ASSIGNED|ACCEPTED|REJECTED|STARTED|IN_PROGRESS|COMPLETED|CANCELLED)$", message = "Status must be ASSIGNED, ACCEPTED, REJECTED, STARTED, IN_PROGRESS, COMPLETED or CANCELLED")
    @Builder.Default
    @Column(nullable = false)
    private String status = "ASSIGNED";

    @Column(name = "otp")
    private String otp;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private ServiceRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    private ServiceProvider provider;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Payment> payments;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Rating> ratings;
}