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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    @Column(nullable = false, length = 10, unique = true)
    private String mobile;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(CUSTOMER|SERVICEPROVIDER|ADMIN)$", message = "Role must be CUSTOMER, SERVICEPROVIDER or ADMIN")
    @Column(nullable = false)
    private String role;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinColumn(name = "id", referencedColumnName = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ServiceProvider serviceProvider;

    @PrePersist
    @PreUpdate
    private void validateUser() {
        if (password != null && password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        try {
            if (!role.matches("^(CUSTOMER|SERVICEPROVIDER|ADMIN)$")) {
                throw new IllegalArgumentException("Invalid role specified");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Role validation failed: " + e.getMessage());
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        try {
            if (!status.matches("^(ACTIVE|INACTIVE)$")) {
                throw new IllegalArgumentException("Invalid status specified");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Status validation failed: " + e.getMessage());
        }
    }
}