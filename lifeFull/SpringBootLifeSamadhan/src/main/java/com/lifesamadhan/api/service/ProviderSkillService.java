package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ProviderSkill;
import com.lifesamadhan.api.repository.ProviderSkillRepository;
import com.lifesamadhan.api.repository.ServiceProviderRepository;
import com.lifesamadhan.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderSkillService {
    
    private final ProviderSkillRepository providerSkillRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    
    public ProviderSkill addSkill(ProviderSkill skill) {
        
        if (!serviceProviderRepository.existsById(skill.getProviderId())) {
            throw new IllegalArgumentException("Provider not found");
        }
        
        
        if (!serviceRepository.existsById(skill.getServiceId())) {
            throw new IllegalArgumentException("Service not found");
        }
        
        
        boolean exists = providerSkillRepository.findByProviderId(skill.getProviderId())
                .stream()
                .anyMatch(ps -> ps.getServiceId().equals(skill.getServiceId()));
        
        if (exists) {
            throw new IllegalArgumentException("Provider already has this skill");
        }
        
        skill.setStatus("PENDING");
        return providerSkillRepository.save(skill);
    }
}