package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.CustomerProfile;
import com.lifesamadhan.api.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {
    
    private final CustomerProfileRepository customerProfileRepository;
    
    public List<CustomerProfile> getAllProfiles() {
        return customerProfileRepository.findAll();
    }
    
    public Optional<CustomerProfile> getProfileById(Long id) {
        return customerProfileRepository.findById(id);
    }
    
    public Optional<CustomerProfile> getProfileByUserId(Long userId) {
        return customerProfileRepository.findByUserId(userId);
    }
    
    public CustomerProfile createProfile(CustomerProfile profile) {
        return customerProfileRepository.save(profile);
    }
    
    public CustomerProfile updateProfile(Long id, CustomerProfile profile) {
        profile.setId(id);
        return customerProfileRepository.save(profile);
    }
    
    public void deleteProfile(Long id) {
        customerProfileRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return customerProfileRepository.existsById(id);
    }
}