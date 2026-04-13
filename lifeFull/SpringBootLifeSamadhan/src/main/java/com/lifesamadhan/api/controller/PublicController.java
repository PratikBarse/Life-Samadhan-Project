package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.model.Service;
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.service.LocationService;
import com.lifesamadhan.api.service.ServiceService;
import com.lifesamadhan.api.service.ServiceProviderService; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final LocationService locationService;
    private final ServiceService serviceService;
    private final ServiceProviderService serviceProviderService;
    private final com.lifesamadhan.api.service.CustomerService customerService;

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/locations/active")
    public ResponseEntity<List<Location>> getActiveLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/services")
    public ResponseEntity<List<Service>> getServices() {
        return ResponseEntity.ok(serviceService.getAllServices());
    }

    @GetMapping("/providers/search")
    public ResponseEntity<?> searchProviders(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String city) {

        if (categoryId != null && city != null) {
            return ResponseEntity.ok(customerService.searchProviders(categoryId, city));
        }

        return ResponseEntity.ok(List.of());
    }
}
