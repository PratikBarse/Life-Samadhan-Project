package com.lifesamadhan.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Entity
@Table(name = "service_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verified = false;

    @NotBlank(message = "Availability is required")
    @Pattern(regexp = "^(AVAILABLE|BUSY|OFFLINE)$", message = "Availability must be AVAILABLE, BUSY or OFFLINE")
    @Builder.Default
    @Column(nullable = false)
    private String availability = "AVAILABLE";

    @NotNull(message = "UserId is required")
    @Min(value = 1, message = "UserId must be a valid value")
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProviderLocation> locations;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProviderSkill> skills;

    @Builder.Default
    @Column(name = "completed_jobs_count", nullable = false)
    private Integer completedJobsCount = 0;

    @Builder.Default
    @Column(name = "rejected_jobs_count", nullable = false)
    private Integer rejectedJobsCount = 0;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "provider_type")
    private String providerType;

    @Column(name = "service_category_id")
    private Long serviceCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ServiceCategory category;
}