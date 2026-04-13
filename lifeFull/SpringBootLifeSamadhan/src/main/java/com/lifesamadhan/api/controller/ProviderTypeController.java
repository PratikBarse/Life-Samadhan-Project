package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.model.ProviderType;
import com.lifesamadhan.api.service.ProviderTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providertype")
@RequiredArgsConstructor
public class ProviderTypeController {

    private final ProviderTypeService providerTypeService;

    @GetMapping("/active")
    public ResponseEntity<List<ProviderType>> getActiveProviderTypes() {
        return ResponseEntity.ok(providerTypeService.getActiveProviderTypes());
    }

    @GetMapping
    public ResponseEntity<List<ProviderType>> getAllProviderTypes() {
        return ResponseEntity.ok(providerTypeService.getAllProviderTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderType> getProviderTypeById(@PathVariable Long id) {
        return providerTypeService.getProviderTypeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProviderType> createProviderType(@RequestBody ProviderType providerType) {
        return ResponseEntity.ok(providerTypeService.createProviderType(providerType));
    }
}
