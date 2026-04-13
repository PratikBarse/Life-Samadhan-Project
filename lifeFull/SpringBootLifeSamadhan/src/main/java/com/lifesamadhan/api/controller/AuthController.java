package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.dto.LoginRequestDTO;
import com.lifesamadhan.api.dto.LoginResponseDTO;
import com.lifesamadhan.api.dto.RegisterRequestDTO; 
import com.lifesamadhan.api.model.CustomerProfile;
import com.lifesamadhan.api.model.Location; 
import com.lifesamadhan.api.model.ProviderLocation; 
import com.lifesamadhan.api.model.ProviderSkill; 
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.model.User;
import com.lifesamadhan.api.security.JwtUtils;
import com.lifesamadhan.api.service.UserService;
import com.lifesamadhan.api.service.ServiceProviderService;
import com.lifesamadhan.api.service.CustomerProfileService;
import com.lifesamadhan.api.service.LocationService;
import com.lifesamadhan.api.service.PasswordService;
import com.lifesamadhan.api.service.ProviderLocationService;
import com.lifesamadhan.api.service.ProviderSkillService; 

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final ServiceProviderService serviceProviderService;
    private final CustomerProfileService customerProfileService;
    private final JwtUtils jwtUtils;
    private final PasswordService passwordService;
    private final ProviderLocationService providerLocationService;
    private final LocationService locationService;
    private final ProviderSkillService providerSkillService; 

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "Auth controller is working"));
    }

    @GetMapping("/test-security")
    public ResponseEntity<?> testSecurity() {
        return ResponseEntity.ok("Security test works");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) { 
        try {
            
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(409)
                        .body(Map.of("message", "Email already exists", "error", "DUPLICATE_EMAIL"));
            }

            
            User user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .mobile(request.getMobile())
                    .password(request.getPassword())
                    .role(request.getRole())
                    .status("ACTIVE")
                    .build();

            
            if (user.getRole() == null || user.getRole().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Role is required", "error", "INVALID_ROLE"));
            }

            if (!user.getRole().matches("^(CUSTOMER|SERVICEPROVIDER|ADMIN)$")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid role. Must be CUSTOMER, SERVICEPROVIDER, or ADMIN", "error",
                                "INVALID_ROLE"));
            }

            
            User savedUser = userService.createUser(user);
            log.info("User registered successfully with ID: {} and role: {}", savedUser.getId(), savedUser.getRole());

            try {
                
                if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
                    CustomerProfile profile = CustomerProfile.builder()
                            .userId(savedUser.getId())
                            .address(request.getAddress()) 
                            .build();
                    customerProfileService.createProfile(profile);
                    log.info("Customer profile created for user ID: {}", savedUser.getId());
                } else if ("SERVICEPROVIDER".equalsIgnoreCase(user.getRole())) {
                    ServiceProvider provider = ServiceProvider.builder()
                            .userId(savedUser.getId())
                            .verified(false)
                            .availability("AVAILABLE")
                            .providerType(request.getProviderType()) 
                            .hourlyRate(request.getHourlyRate()) 
                            .serviceCategoryId(request.getServiceCategoryId()) 
                            .build();

                    ServiceProvider savedProvider = serviceProviderService.createProvider(provider);
                    log.info("Service provider profile created for user ID: {}", savedUser.getId());

                    
                    if (request.getLocationId() != null && request.getLocationId() > 0) {
                        try {
                            if (locationService.existsById(request.getLocationId())) {
                                ProviderLocation provLoc = ProviderLocation.builder()
                                        .providerId(savedProvider.getId())
                                        .locationId(request.getLocationId())
                                        .status("APPROVED") 
                                        .build();
                                providerLocationService.addLocation(provLoc);
                            } else {
                                log.warn("Location ID {} not found, skipping provider location linkage",
                                        request.getLocationId());
                            }
                        } catch (Exception locEx) {
                            log.error("Failed to add location for provider", locEx);
                        }
                    }

                    
                    if (request.getSkills() != null && !request.getSkills().trim().isEmpty()) {
                        String[] skillIds = request.getSkills().split(",");
                        for (String skillIdStr : skillIds) {
                            try {
                                Long serviceId = Long.parseLong(skillIdStr.trim());
                                if (serviceId > 0) {
                                    ProviderSkill skill = ProviderSkill.builder()
                                            .providerId(savedProvider.getId())
                                            .serviceId(serviceId)
                                            .status("PENDING")
                                            .build();
                                    try {
                                        providerSkillService.addSkill(skill);
                                    } catch (Exception e) {
                                        log.warn("Failed to add skill {} for provider: {}", serviceId, e.getMessage());
                                    }
                                }
                            } catch (NumberFormatException nfe) {
                                
                            }
                        }
                    }
                }
            } catch (Exception profileException) {
                log.error("Error creating user profile for user ID: {}", savedUser.getId(), profileException);
                
                return ResponseEntity.status(206) 
                        .body(Map.of(
                                "message", "User registered but profile creation failed",
                                "error", "PROFILE_CREATION_FAILED",
                                "userId", savedUser.getId(),
                                "details", profileException.getMessage()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("role", savedUser.getRole());

            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid registration request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid input: " + e.getMessage(), "error", "INVALID_INPUT"));
        } catch (Exception e) {
            log.error("Unexpected error during registration: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "message", "Registration failed due to server error",
                            "error", "SERVER_ERROR",
                            "details", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email is required", "error", "MISSING_EMAIL"));
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Password is required", "error", "MISSING_PASSWORD"));
            }

            
            User user = userService.getUserByEmail(request.getEmail())
                    .orElse(null);

            if (user == null) {
                log.warn("Login attempt with non-existent email: {}", request.getEmail());
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid email or password", "error", "INVALID_CREDENTIALS"));
            }

            
            if (!passwordService.verify(request.getPassword(), user.getPassword())) {
                log.warn("Failed login attempt for email: {} - invalid password", request.getEmail());
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid email or password", "error", "INVALID_CREDENTIALS"));
            }

            
            if ("INACTIVE".equalsIgnoreCase(user.getStatus())) {
                log.warn("Login attempt for inactive user: {}", request.getEmail());
                return ResponseEntity.status(403)
                        .body(Map.of("message", "User account is inactive", "error", "USER_INACTIVE"));
            }

            
            String token = jwtUtils.generateToken(user.getEmail(), user.getId(), user.getRole());

            
            String location = "Location not set";
            Long providerId = null;
            Long customerProfileId = null;

            if ("SERVICEPROVIDER".equalsIgnoreCase(user.getRole())) {
                try {
                    ServiceProvider provider = serviceProviderService.getProviderByUserId(user.getId()).orElse(null);
                    if (provider != null) {
                        providerId = provider.getId();
                        try {
                            Long locationId = providerLocationService.getPrimaryLocationId(provider.getId());
                            if (locationId == null) {
                                
                                locationId = providerLocationService.getAnyLocationId(provider.getId());
                            }
                            if (locationId != null) {
                                location = locationService.getLocationStringById(locationId);
                            }
                        } catch (Exception locException) {
                            log.warn("Could not fetch location for provider ID: {}", providerId, locException);
                            location = "Location not set";
                        }
                    } else {
                        log.info("No ServiceProvider record found for user ID: {}", user.getId());
                    }
                } catch (Exception providerException) {
                    log.warn("Could not fetch provider details for user ID: {}", user.getId(), providerException);
                    location = "Location not set";
                }
            } else if ("CUSTOMER".equalsIgnoreCase(user.getRole())) {
                try {
                    CustomerProfile profile = customerProfileService.getProfileByUserId(user.getId()).orElse(null);
                    if (profile != null) {
                        customerProfileId = profile.getId();
                        location = profile.getAddress() != null ? profile.getAddress() : "Location not set";
                    }
                } catch (Exception e) {
                    location = "Location not set";
                }
            }

            LoginResponseDTO response = LoginResponseDTO.builder()
                    .token(token)
                    .role(user.getRole())
                    .name(user.getName())
                    .email(user.getEmail())
                    .location(location)
                    .userId(user.getId())
                    .providerId(providerId)
                    .customerProfileId(customerProfileId)
                    .message("Login successful")
                    .build();

            log.info("User logged in successfully: {} with role: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error during login: ", e);
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "message", "Login failed due to server error",
                            "error", "SERVER_ERROR"));
        }
    }
}