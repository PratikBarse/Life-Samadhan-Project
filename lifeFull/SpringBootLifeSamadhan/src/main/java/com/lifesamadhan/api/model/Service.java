package com.lifesamadhan.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "services", uniqueConstraints = @UniqueConstraint(columnNames = { "category_id", "name" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "CategoryId is required")
    @Min(value = 1, message = "CategoryId must be a valid value")
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @NotBlank(message = "Service name is required")
    @Size(min = 3, max = 150, message = "Service name must be between 3 and 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @JsonIgnore
    private ServiceCategory category;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<ProviderSkill> providerSkills;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private java.util.List<ServiceRequest> serviceRequests;
}