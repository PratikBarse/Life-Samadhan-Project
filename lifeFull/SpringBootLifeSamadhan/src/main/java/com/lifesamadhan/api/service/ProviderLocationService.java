package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.repository.ProviderLocationRepository;
import com.lifesamadhan.api.repository.UserRepository;
import com.lifesamadhan.api.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderLocationService {
    
    private final ProviderLocationRepository providerLocationRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    
    public ProviderLocation addLocation(ProviderLocation location) {
        
        if (!userRepository.existsById(location.getProviderId())) {
            throw new IllegalArgumentException("Provider not found");
        }
        
        
        if (!locationRepository.existsById(location.getLocationId())) {
            throw new IllegalArgumentException("Location not found");
        }
        
        
        boolean exists = providerLocationRepository.findByProviderId(location.getProviderId())
                .stream()
                .anyMatch(pl -> pl.getLocationId().equals(location.getLocationId()));
        
        if (exists) {
            throw new IllegalArgumentException("Provider already has this location");
        }
        
        location.setStatus("PENDING");
        return providerLocationRepository.save(location);
    }
    
    public Long getPrimaryLocationId(Long providerId) {
        return providerLocationRepository.findByProviderId(providerId)
            .stream()
            .filter(pl -> "ACTIVE".equals(pl.getStatus()) || "APPROVED".equals(pl.getStatus()) || "PENDING".equals(pl.getStatus()))
            .findFirst()
            .map(ProviderLocation::getLocationId)
            .orElse(null);
    }

    
    public Long getAnyLocationId(Long providerId) {
        return providerLocationRepository.findByProviderId(providerId)
                .stream()
                .findFirst()
                .map(ProviderLocation::getLocationId)
                .orElse(null);
    }
}