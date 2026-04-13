package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.model.User;
import com.lifesamadhan.api.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceProviderService {
    
    private final ServiceProviderRepository serviceProviderRepository;
    
    public List<ServiceProvider> getAllProviders() {
        return serviceProviderRepository.findAll();
    }
    
    public Optional<ServiceProvider> getProviderById(Long id) {
        return serviceProviderRepository.findById(id);
    }
    
    public Optional<ServiceProvider> getProviderByUserId(Long userId) {
        return serviceProviderRepository.findByUserId(userId);
    }
    
    public ServiceProvider createProvider(ServiceProvider provider) {
        return serviceProviderRepository.save(provider);
    }
    
    public ServiceProvider updateProvider(Long id, ServiceProvider provider) {
        provider.setId(id);
        return serviceProviderRepository.save(provider);
    }
    
    public ServiceProvider updateAvailability(Long providerId, String availability) {
        ServiceProvider provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        provider.setAvailability(availability);
        return serviceProviderRepository.save(provider);
    }
    
    public void deleteProvider(Long id) {
        serviceProviderRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return serviceProviderRepository.existsById(id);
    }

	
}