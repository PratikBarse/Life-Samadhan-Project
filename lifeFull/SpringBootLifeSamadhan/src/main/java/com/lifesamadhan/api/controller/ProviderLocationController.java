package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.dto.CreateProviderLocationDTO;
import com.lifesamadhan.api.dto.ProviderLocationResponseDTO;
import com.lifesamadhan.api.dto.DTOMapper;
import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.service.ProviderLocationService;
import com.lifesamadhan.api.service.ServiceProviderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider/location")
@RequiredArgsConstructor
public class ProviderLocationController {
    
    private final ProviderLocationService providerLocationService;
    private final ServiceProviderService serviceProviderService;
    private final DTOMapper dtoMapper;
    
    @PostMapping("/add")
    public ResponseEntity<ProviderLocationResponseDTO> addLocation(@Valid @RequestBody CreateProviderLocationDTO locationDTO) {
        Optional<ServiceProvider> serviceProvider = serviceProviderService.getProviderByUserId(locationDTO.getProviderId());
        if (serviceProvider.isPresent()) {
            locationDTO.setProviderId(serviceProvider.get().getId());
        }
        ProviderLocation location = dtoMapper.toProviderLocation(locationDTO);
        ProviderLocation savedLocation = providerLocationService.addLocation(location);
        return ResponseEntity.ok(dtoMapper.toProviderLocationResponseDTO(savedLocation));
    }
}