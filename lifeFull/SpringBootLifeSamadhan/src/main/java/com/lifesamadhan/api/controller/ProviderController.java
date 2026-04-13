package com.lifesamadhan.api.controller;

import com.lifesamadhan.api.dto.DTOMapper;
import com.lifesamadhan.api.model.ServiceAssignment;
import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.security.JwtUtils;
import com.lifesamadhan.api.service.ProviderService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor

public class ProviderController {

    private final ProviderService providerService;
    private final JwtUtils jwtUtil;
    private final DTOMapper dtoMapper;

    @PutMapping("/update-availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Boolean isAvailableObj = request.get("isAvailable");
            if (isAvailableObj == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "isAvailable field is required"));
            }

            boolean isAvailable = isAvailableObj;
            providerService.updateProviderAvailability(username, isAvailable);

            return ResponseEntity.ok(Map.of("message", "Availability updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/assignment/{assignmentId}/accept")
    public ResponseEntity<?> acceptAssignment(@PathVariable Long assignmentId) {
        try {
            if (assignmentId == null || assignmentId <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid assignment ID"));
            }

            ServiceAssignment assignment = providerService.acceptAssignment(assignmentId);
            return ResponseEntity.ok(dtoMapper.toLegacyAssignmentMap(assignment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid request: " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Operation not allowed: " + e.getMessage()));
        } catch (RuntimeException e) {
            
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "An unexpected error occurred"));
        }
    }

    @PostMapping("/assignment/{assignmentId}/reject")
    public ResponseEntity<?> rejectAssignment(@PathVariable Long assignmentId) {
        try {
            ServiceAssignment assignment = providerService.rejectAssignment(assignmentId);
            return ResponseEntity.ok(dtoMapper.toLegacyAssignmentMap(assignment));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/assignment/{assignmentId}/start")
    public ResponseEntity<?> startService(@PathVariable Long assignmentId,
            @RequestBody(required = false) String otpPayload) {
        try {
            System.out.println("DEBUG: startService called waiting for ID: " + assignmentId);
            System.out.println("DEBUG: Received Payload: " + otpPayload);

            if (otpPayload == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "OTP is required"));
            }

            
            
            String otp = otpPayload.replaceAll("\\D", "");
            System.out.println("DEBUG: Sanitized OTP: " + otp);

            if (otp.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "OTP must contain digits"));
            }

            ServiceAssignment assignment = providerService.startService(assignmentId, otp);
            return ResponseEntity.ok(dtoMapper.toLegacyAssignmentMap(assignment));
        } catch (RuntimeException e) {
            System.err.println("ERROR in startService: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/assignment/{assignmentId}/complete")
    public ResponseEntity<?> completeService(@PathVariable Long assignmentId) {
        try {
            ServiceAssignment assignment = providerService.completeService(assignmentId);
            return ResponseEntity.ok(dtoMapper.toLegacyAssignmentMap(assignment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getMyAssignments(HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            var assignments = providerService.getAssignmentsByUsername(username);
            java.util.List<Map<String, Object>> legacyMaps = assignments.stream()
                    .map(dtoMapper::toLegacyAssignmentMap)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(legacyMaps);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/earnings")
    public ResponseEntity<?> getEarnings(HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            var earnings = providerService.getProviderEarnings(username);
            return ResponseEntity.ok(earnings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/assignments/{providerId}")
    public ResponseEntity<?> getAssignments(@PathVariable Long providerId) {
        try {
            var assignments = providerService.getAssignmentsByProviderId(providerId);
            java.util.List<Map<String, Object>> legacyMaps = assignments.stream()
                    .map(dtoMapper::toLegacyAssignmentMap)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(legacyMaps);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{providerId}/skills")
    public ResponseEntity<?> getProviderSkills(@PathVariable Long providerId) {
        try {
            var skills = providerService.getSkillsByProviderId(providerId);
            return ResponseEntity.ok(skills.stream().map(dtoMapper::toSkillResponseDTO).toList());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/set-location")
    public ResponseEntity<?> setLocation(@RequestBody(required = false) Map<String, Long> request,
            HttpServletRequest httpRequest) {
        try {
            if (request == null || request.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Request body is required with locationId"));
            }

            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Long locationId = request.get("locationId");
            if (locationId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "locationId is required"));
            }

            ProviderLocation providerLocation = providerService.setProviderLocation(username, locationId);
            return ResponseEntity.ok(providerLocation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/location")
    public ResponseEntity<?> getProviderLocation(HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            String location = providerService.getProviderLocation(username);
            return ResponseEntity.ok(Map.of("location", location));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProviderDetails(HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            com.lifesamadhan.api.model.User user = providerService.getUserByEmail(username);
            com.lifesamadhan.api.model.ServiceProvider provider = providerService.getProviderByUserId(user.getId());

            return ResponseEntity.ok(Map.of(
                    "providerId", provider.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "availability", provider.getAvailability()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/available-locations")
    @PreAuthorize("hasRole('SERVICEPROVIDER') or hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<?> getLocations() {
        try {
            var locations = providerService.getAllActiveLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}