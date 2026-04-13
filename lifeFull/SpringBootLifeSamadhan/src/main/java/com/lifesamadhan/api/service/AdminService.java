package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ProviderSkill;
import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.model.User;
import com.lifesamadhan.api.repository.ProviderSkillRepository;
import com.lifesamadhan.api.repository.ProviderLocationRepository;
import com.lifesamadhan.api.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserService userService;
    private final ProviderSkillRepository providerSkillRepository;
    private final ProviderLocationRepository providerLocationRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public Optional<ProviderSkill> approveSkill(Long id) {
        Optional<ProviderSkill> skillOpt = providerSkillRepository.findById(id);
        if (skillOpt.isPresent()) {
            ProviderSkill skill = skillOpt.get();
            skill.setStatus("APPROVED");
            ProviderSkill approvedSkill = providerSkillRepository.save(skill);

            
            Optional<ServiceProvider> providerOpt = serviceProviderRepository.findById(skill.getProviderId());
            if (providerOpt.isPresent()) {
                ServiceProvider provider = providerOpt.get();
                provider.setVerified(true);
                serviceProviderRepository.save(provider);
                log.info("Service provider {} marked as verified after skill approval", provider.getId());
            }

            return Optional.of(approvedSkill);
        }
        return Optional.empty();
    }

    public Optional<ProviderLocation> approveLocation(Long id) {
        Optional<ProviderLocation> locationOpt = providerLocationRepository.findById(id);
        if (locationOpt.isPresent()) {
            ProviderLocation location = locationOpt.get();
            location.setStatus("APPROVED");
            ProviderLocation approvedLocation = providerLocationRepository.save(location);

            
            Optional<ServiceProvider> providerOpt = serviceProviderRepository.findById(location.getProviderId());
            if (providerOpt.isPresent()) {
                ServiceProvider provider = providerOpt.get();
                provider.setVerified(true);
                serviceProviderRepository.save(provider);
                log.info("Service provider {} marked as verified after location approval", provider.getId());
            }

            return Optional.of(approvedLocation);
        }
        return Optional.empty();
    }

    public List<ProviderSkill> getSkillsByStatus(String status) {
        return providerSkillRepository.findByStatus(status);
    }

    public List<ProviderLocation> getLocationsByStatus(String status) {
        return providerLocationRepository.findByStatus(status);
    }

    public List<ServiceProvider> getAllProviders() {
        return serviceProviderRepository.findAll();
    }

    public Optional<ServiceProvider> verifyProvider(Long id) {
        Optional<ServiceProvider> providerOpt = serviceProviderRepository.findById(id);
        if (providerOpt.isPresent()) {
            ServiceProvider provider = providerOpt.get();
            provider.setVerified(true);
            return Optional.of(serviceProviderRepository.save(provider));
        }
        return Optional.empty();
    }

    public Optional<ServiceProvider> updateProviderStatus(Long id, String status) {
        Optional<ServiceProvider> providerOpt = serviceProviderRepository.findById(id);
        if (providerOpt.isPresent()) {
            ServiceProvider provider = providerOpt.get();
            if (provider.getUser() != null) {
                User user = provider.getUser();
                user.setStatus(status);
                userService.updateUser(user.getId(), user);
            }
            return Optional.of(provider);
        }
        return Optional.empty();
    }

    public boolean deleteProvider(Long id) {
        Optional<ServiceProvider> providerOpt = serviceProviderRepository.findById(id);
        if (providerOpt.isPresent()) {
            ServiceProvider provider = providerOpt.get();
            
            userService.deleteUser(provider.getUserId());
            return true;
        }
        return false;
    }
}