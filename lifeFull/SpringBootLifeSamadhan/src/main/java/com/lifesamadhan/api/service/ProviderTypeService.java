package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ProviderType;
import com.lifesamadhan.api.repository.ProviderTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProviderTypeService {

    private final ProviderTypeRepository providerTypeRepository;

    public List<ProviderType> getAllProviderTypes() {
        return providerTypeRepository.findAll();
    }

    public List<ProviderType> getActiveProviderTypes() {
        return providerTypeRepository.findByStatus("ACTIVE");
    }

    public Optional<ProviderType> getProviderTypeById(Long id) {
        return providerTypeRepository.findById(id);
    }

    public ProviderType createProviderType(ProviderType providerType) {
        return providerTypeRepository.save(providerType);
    }

    public ProviderType updateProviderType(Long id, ProviderType updatedType) {
        if (!providerTypeRepository.existsById(id)) {
            throw new RuntimeException("Provider Type not found");
        }
        updatedType.setId(id);
        return providerTypeRepository.save(updatedType);
    }

    public void deleteProviderType(Long id) {
        providerTypeRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return providerTypeRepository.existsById(id);
    }
}
