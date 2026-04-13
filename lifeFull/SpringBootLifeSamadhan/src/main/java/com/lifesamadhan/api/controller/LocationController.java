package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return locationService.getLocationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        Location savedLocation = locationService.createLocation(location);
        return ResponseEntity.ok(savedLocation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id,
            @Valid @RequestBody Location location) {
        if (!locationService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        if (locationService.existsById(id)) {
            locationService.deleteLocation(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        List<String> countries = locationService.getCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/states/{country}")
    public ResponseEntity<List<String>> getStatesByCountry(@PathVariable String country) {
        List<String> states = locationService.getStatesByCountry(country);
        return ResponseEntity.ok(states);
    }

    @GetMapping("/districts/{state}")
    public ResponseEntity<List<String>> getDistrictsByState(@PathVariable String state) {
        List<String> districts = locationService.getDistrictsByState(state);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/pincodes/{district}")
    public ResponseEntity<List<String>> getPincodesByDistrict(@PathVariable String district) {
        List<String> pincodes = locationService.getPincodesByDistrict(district);
        return ResponseEntity.ok(pincodes);
    }
}