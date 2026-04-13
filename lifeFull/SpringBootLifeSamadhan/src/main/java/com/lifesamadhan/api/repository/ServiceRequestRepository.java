package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "service", "customer.user" })
    java.util.Optional<ServiceRequest> findById(Long id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "service", "customer.user" })
    List<ServiceRequest> findByCustomerId(Long customerId);

    List<ServiceRequest> findByServiceId(Long serviceId);

    List<ServiceRequest> findByStatus(String status);
}