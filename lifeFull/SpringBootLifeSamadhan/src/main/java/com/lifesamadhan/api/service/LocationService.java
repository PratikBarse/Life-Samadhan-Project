package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    public List<Location> getAllLocations() {
        return locationRepository.findByStatus("ACTIVE");
    }
    
    public List<Location> getAllLocationsForAdmin() {
        return locationRepository.findAll();
    }
    
    public String getLocationStringById(Long locationId) {
        Optional<Location> location = locationRepository.findById(locationId);
        if (location.isPresent()) {
            Location loc = location.get();
            return loc.getDistrict() + ", " + loc.getState();
        }
        return "Unknown Location";
    }
    
    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }
    
    public Location createLocation(Location location) {
        location.setStatus("ACTIVE");
        return locationRepository.save(location);
    }
    
    public Location updateLocation(Long id, Location location) {
        location.setId(id);
        return locationRepository.save(location);
    }
    
    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }
    
    public boolean existsById(Long id) {
        return locationRepository.existsById(id);
    }
    
    public List<String> getCountries() {
        return locationRepository.findDistinctCountries();
    }
    
    public List<String> getStatesByCountry(String country) {
        return locationRepository.findDistinctStatesByCountry(country);
    }
    
    public List<String> getDistrictsByState(String state) {
        return locationRepository.findDistinctDistrictsByState(state);
    }
    
    public List<String> getPincodesByDistrict(String district) {
        return locationRepository.findDistinctPincodesByDistrict(district);
    }
}