package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.dto.CreateProviderSkillDTO;
import com.lifesamadhan.api.dto.SkillResponseDTO;
import com.lifesamadhan.api.dto.DTOMapper;
import com.lifesamadhan.api.model.ProviderSkill;
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.service.ProviderSkillService;
import com.lifesamadhan.api.service.ServiceProviderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider/skill")
@RequiredArgsConstructor
@Slf4j
public class ProviderSkillController {
    
    private final ProviderSkillService providerSkillService;
    private final ServiceProviderService serviceProviderService;
    private final DTOMapper dtoMapper;
    
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<SkillResponseDTO> addSkill(@RequestBody CreateProviderSkillDTO skillDTO) {
        log.info("Received skill DTO: {}", skillDTO);
        log.info("Hourly rate from DTO: {}", skillDTO.getHourlyRate());

        
        if (skillDTO == null) {
            log.warn("Skill DTO is null");
            return ResponseEntity.badRequest().build();
        }
        if (skillDTO.getProviderId() == null || skillDTO.getProviderId() <= 0) {
            log.warn("Invalid providerId in DTO: {}", skillDTO.getProviderId());
            return ResponseEntity.badRequest().body(null);
        }
        if (skillDTO.getServiceId() == null || skillDTO.getServiceId() <= 0) {
            log.warn("Invalid serviceId in DTO: {}", skillDTO.getServiceId());
            return ResponseEntity.badRequest().body(null);
        }
        
        
        Optional<ServiceProvider> serviceProviderOpt = serviceProviderService.getProviderByUserId(skillDTO.getProviderId());
        ServiceProvider provider;
        
        if (serviceProviderOpt.isPresent()) {
            provider = serviceProviderOpt.get();
            log.info("Found existing provider: {}", provider.getId());
        } else {
            
            provider = ServiceProvider.builder()
                    .userId(skillDTO.getProviderId())
                    .verified(false)
                    .availability("AVAILABLE")
                    .completedJobsCount(0)
                    .rejectedJobsCount(0)
                    .build();
            provider = serviceProviderService.createProvider(provider);
            log.info("Created new provider: {} (userId={})", provider.getId(), provider.getUserId());
        }
        
        
        if (skillDTO.getHourlyRate() != null) {
            provider.setHourlyRate(skillDTO.getHourlyRate());
            try {
                ServiceProvider updatedProvider = serviceProviderService.updateProvider(provider.getId(), provider);
                log.info("Updated provider hourly rate to: {} for providerId={}", updatedProvider.getHourlyRate(), updatedProvider.getId());
            } catch (Exception e) {
                log.error("Failed to update provider hourly rate for providerId={}", provider.getId(), e);
                throw e;
            }
        } else {
            log.info("No hourly rate provided in DTO; skipping provider hourly rate update");
        }
        
        
        skillDTO.setProviderId(provider.getId());
        
        ProviderSkill skill = dtoMapper.toProviderSkill(skillDTO);
        ProviderSkill savedSkill = providerSkillService.addSkill(skill);
        return ResponseEntity.ok(dtoMapper.toSkillResponseDTO(savedSkill));
    }
}