package com.lifesamadhan.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "provider_skills", uniqueConstraints = @UniqueConstraint(columnNames = { "provider_id", "service_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "ProviderId is required")
    @Min(value = 1, message = "ProviderId must be a valid value")
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @NotNull(message = "ServiceId is required")
    @Min(value = 1, message = "ServiceId must be a valid value")
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|PENDING|APPROVED)$", message = "Status must be ACTIVE, INACTIVE, PENDING, or APPROVED")
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ServiceProvider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private Service service;

    @PrePersist
    @PreUpdate
    private void validateEntity() {
        if (providerId == null || providerId <= 0) {
            throw new IllegalArgumentException("Provider ID must be a positive number");
        }
        if (serviceId == null || serviceId <= 0) {
            throw new IllegalArgumentException("Service ID must be a positive number");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
    }
}
