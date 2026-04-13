package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ServiceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceAssignmentRepository extends JpaRepository<ServiceAssignment, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "request", "provider", "request.service",
            "provider.user" })
    java.util.Optional<ServiceAssignment> findById(Long id);

    
    List<ServiceAssignment> findByRequestId(Long requestId);

    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "request", "provider", "request.service",
            "provider.user" })
    List<ServiceAssignment> findByProviderId(Long providerId);

    
    List<ServiceAssignment> findByStatus(String status);

    List<ServiceAssignment> findByStatusAndAssignedAtBefore(String status, java.time.LocalDateTime cutoffTime);
}