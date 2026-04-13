package com.lifesamadhan.api.repository;

import com.lifesamadhan.api.model.ProviderLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderLocationRepository extends JpaRepository<ProviderLocation, Long> {
    List<ProviderLocation> findByProviderId(Long providerId);
    List<ProviderLocation> findByLocationId(Long locationId);
    List<ProviderLocation> findByStatus(String status);
}