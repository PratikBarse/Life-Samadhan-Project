package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.Service;
import com.lifesamadhan.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;


import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {
    
    private final ServiceRepository serviceRepository;
    
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }
    
    public Optional<Service> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }
    
    public List<Service> getServicesByCategory(Long categoryId) {
        return serviceRepository.findByCategoryId(categoryId);
    }
    
    public List<Service> getServicesByStatus(String status) {
        return serviceRepository.findByStatus(status);
    }
    
    public Service createService(Service service) {
        return serviceRepository.save(service);
    }
    
    public Service updateService(Long id, Service service) {
        service.setId(id);
        return serviceRepository.save(service);
    }
    
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return serviceRepository.existsById(id);
    }
}