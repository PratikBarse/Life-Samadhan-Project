package com.lifesamadhan.api.service;

import com.lifesamadhan.api.model.ServiceRequest;
import com.lifesamadhan.api.model.ServiceAssignment;
import com.lifesamadhan.api.model.Rating;
import com.lifesamadhan.api.model.User;
import com.lifesamadhan.api.model.CustomerProfile;
import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.repository.ServiceRequestRepository;
import com.lifesamadhan.api.repository.ServiceAssignmentRepository;
import com.lifesamadhan.api.repository.RatingRepository;
import com.lifesamadhan.api.repository.UserRepository;
import com.lifesamadhan.api.repository.CustomerProfileRepository;
import com.lifesamadhan.api.repository.LocationRepository;
import com.lifesamadhan.api.repository.ProviderSkillRepository;
import com.lifesamadhan.api.repository.ProviderLocationRepository;
import com.lifesamadhan.api.repository.ServiceProviderRepository;
import com.lifesamadhan.api.model.ProviderSkill;
import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.model.ServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceAssignmentRepository serviceAssignmentRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final LocationRepository locationRepository;
    private final ProviderSkillRepository providerSkillRepository;
    private final ProviderLocationRepository providerLocationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final com.lifesamadhan.api.repository.ServiceRepository serviceRepository;
    private final com.lifesamadhan.api.repository.ServiceCategoryRepository serviceCategoryRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.lifesamadhan.api.dto.DTOMapper dtoMapper;
    private final EmailService emailService;

    public List<com.lifesamadhan.api.dto.ProviderDTO> findAvailableProviders(Long serviceId, Long locationId) {
        log.info("Finding available providers for Service: {} in Location: {}", serviceId, locationId);

        List<ProviderSkill> skilledProviders = providerSkillRepository.findByServiceIdAndStatus(serviceId, "APPROVED");
        Set<Long> skilledProviderIds = skilledProviders.stream()
                .map(ProviderSkill::getProviderId)
                .collect(Collectors.toSet());

        List<ProviderLocation> locationProviders = providerLocationRepository.findByLocationId(locationId);
        Set<Long> locationProviderIds = locationProviders.stream()
                .filter(pl -> "APPROVED".equals(pl.getStatus()) || "ACTIVE".equals(pl.getStatus()))
                .map(ProviderLocation::getProviderId)
                .collect(Collectors.toSet());

        skilledProviderIds.retainAll(locationProviderIds);

        if (skilledProviderIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ServiceProvider> providers = serviceProviderRepository.findAllById(skilledProviderIds).stream()
                .filter(sp -> Boolean.TRUE.equals(sp.getVerified()))
                .collect(Collectors.toList());
        List<com.lifesamadhan.api.dto.ProviderDTO> result = new ArrayList<>();

        for (ServiceProvider sp : providers) {

            User user = userRepository.findById(sp.getUserId()).orElse(null);
            if (user == null)
                continue;

            Double avgRating = ratingRepository.getAverageRatingForProvider(sp.getId());

            result.add(com.lifesamadhan.api.dto.ProviderDTO.builder()
                    .id(sp.getId())
                    .name(user.getName())
                    .hourlyRate(sp.getHourlyRate())
                    .rating(avgRating != null ? avgRating : 0.0)
                    .completedJobs(sp.getCompletedJobsCount())
                    .build());
        }

        result.sort((p1, p2) -> Double.compare(p2.getRating(), p1.getRating()));

        return result;
    }

    public com.lifesamadhan.api.model.Service getFirstServiceByCategoryId(Long categoryId) {
        return serviceRepository.findByCategoryId(categoryId).stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public Long resolveServiceId(Long categoryId, Long providerId) {
        log.info("Resolving serviceId for Category: {} and Provider: {}", categoryId, providerId);

        com.lifesamadhan.api.model.Service existingSvc = getFirstServiceByCategoryId(categoryId);
        if (existingSvc != null) {
            log.info("Resolved existing serviceId: {}", existingSvc.getId());
            return existingSvc.getId();
        }

        if (providerId != null) {
            List<ProviderSkill> skills = providerSkillRepository.findByProviderId(providerId);
            Long skillSvcId = skills.stream()
                    .map(ProviderSkill::getServiceId)
                    .filter(id -> {
                        com.lifesamadhan.api.model.Service s = serviceRepository.findById(id).orElse(null);
                        return s != null && categoryId.equals(s.getCategoryId());
                    })
                    .findFirst()
                    .orElse(null);
            if (skillSvcId != null) {
                log.info("Resolved serviceId {} from provider's existing skills", skillSvcId);
                return skillSvcId;
            }
        }

        return serviceCategoryRepository.findById(categoryId).map(cat -> {
            log.info("Creating fallback 'General' service for category: {}", cat.getName());
            com.lifesamadhan.api.model.Service newService = com.lifesamadhan.api.model.Service.builder()
                    .categoryId(categoryId)
                    .name("General " + cat.getName())
                    .status("ACTIVE")
                    .build();
            com.lifesamadhan.api.model.Service saved = serviceRepository.save(newService);
            log.info("Created new serviceId: {}", saved.getId());
            return saved.getId();
        }).orElseGet(() -> {
            log.warn("Category ID {} not found in database", categoryId);
            return null;
        });
    }

    public ServiceAssignment createServiceRequest(ServiceRequest request, Long specificProviderId) {
        log.info("Creating service request for Customer: {}, Service: {}", request.getCustomerId(),
                request.getServiceId());

        CustomerProfile customerProfile = customerProfileRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        Long locationId = request.getLocationId();

        // Validate if the provided location exists
        if (locationId != null && !locationRepository.existsById(locationId)) {
            log.warn("Provided locationId {} does not exist. Falling back to profile location.", locationId);
            locationId = null;
        }

        if (locationId == null) {
            locationId = customerProfile.getLocationId();
        }

        // Final fallback: If still null or invalid, pick any valid location from DB
        if (locationId == null || !locationRepository.existsById(locationId)) {
            locationId = locationRepository.findAll().stream()
                    .findFirst()
                    .map(Location::getId)
                    .orElse(null);
        }

        request.setLocationId(locationId);

        if (locationId == null) {
            throw new RuntimeException("No valid location found in database. Please add locations first.");
        }

        ServiceProvider selectedProvider = null;

        if (specificProviderId != null) {

            log.info("Manual provider selection: {}", specificProviderId);
            selectedProvider = serviceProviderRepository.findById(specificProviderId)
                    .orElseThrow(() -> new RuntimeException("Selected provider not found"));

        } else {

            throw new RuntimeException("Please select a provider to continue.");
        }

        com.lifesamadhan.api.model.Service svcObj = serviceRepository.findById(request.getServiceId()).orElse(null);
        request.setService(svcObj);
        if (selectedProvider != null) {
            request.setProviderId(selectedProvider.getId());
        }
        ServiceRequest savedRequest = serviceRequestRepository.saveAndFlush(request);

        ServiceAssignment assignment = ServiceAssignment.builder()
                .requestId(savedRequest.getId())
                .providerId(selectedProvider.getId())
                .status("ASSIGNED")
                .otp(String.format("%04d", new java.util.Random().nextInt(10000)))
                .build();

        assignment.setRequest(savedRequest);
        assignment.setProvider(selectedProvider);

        ServiceAssignment savedAssignment = serviceAssignmentRepository.saveAndFlush(assignment);

        try {
            User customerUser = userRepository.findById(customerProfile.getUserId()).orElse(null);
            User providerUser = userRepository.findById(selectedProvider.getUserId()).orElse(null);
            com.lifesamadhan.api.model.Service svc = serviceRepository.findById(request.getServiceId()).orElse(null);

            if (customerUser != null && providerUser != null && svc != null) {
                emailService.sendOtpEmail(
                        customerUser.getEmail(),
                        savedAssignment.getOtp(),
                        providerUser.getName(),
                        svc.getName());
            }
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
        }

        messagingTemplate.convertAndSend("/topic/provider-" + selectedProvider.getId(),
                dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));

        return serviceAssignmentRepository.findById(savedAssignment.getId()).orElse(savedAssignment);
    }

    public ServiceAssignment createServiceRequest(ServiceRequest request) {
        return createServiceRequest(request, null);
    }

    public List<ServiceAssignment> getCustomerBookings(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        List<ServiceRequest> customerRequests = serviceRequestRepository.findByCustomerId(profile.getId());

        List<ServiceAssignment> assignments = new java.util.ArrayList<>();
        for (ServiceRequest request : customerRequests) {
            assignments.addAll(serviceAssignmentRepository.findByRequestId(request.getId()));
        }

        return assignments;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public CustomerProfile saveCustomerProfile(CustomerProfile profile) {
        return customerProfileRepository.save(profile);
    }

    public CustomerProfile getCustomerProfileByUserId(Long userId) {
        return customerProfileRepository.findByUserId(userId)
                .orElse(null);
    }

    public ServiceAssignment cancelRequest(Long assignmentId) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if ("CANCELLED".equals(assignment.getStatus())) {
            throw new RuntimeException("This request is already cancelled");
        }

        if ("STARTED".equals(assignment.getStatus()) || "COMPLETED".equals(assignment.getStatus())) {
            throw new RuntimeException("Service already started or completed (Status: " + assignment.getStatus()
                    + ") - cancellation not allowed");
        }

        assignment.setStatus("CANCELLED");
        ServiceAssignment savedAssignment = serviceAssignmentRepository.save(assignment);
        Object dto = dtoMapper.toServiceAssignmentResponseDTO(savedAssignment);

        // Global Notification (Fail-safe)
        messagingTemplate.convertAndSend("/topic/notifications", dto);

        if (assignment.getRequest() != null) {
            ServiceRequest req = assignment.getRequest();
            req.setStatus("CANCELLED");
            serviceRequestRepository.save(req);
        }

        messagingTemplate.convertAndSend("/topic/request-" + assignment.getRequestId(), dto);

        if (assignment.getProviderId() != null) {
            messagingTemplate.convertAndSend("/topic/provider-" + assignment.getProviderId(), dto);
        }

        if (assignment.getRequest() != null) {
            Long customerId = assignment.getRequest().getCustomerId();
            messagingTemplate.convertAndSend("/topic/customer-" + customerId,
                    dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));

            // Also notify via universal User ID if available
            try {
                com.lifesamadhan.api.model.CustomerProfile cp = customerProfileRepository.findById(customerId)
                        .orElse(null);
                if (cp != null && cp.getUserId() != null) {
                    messagingTemplate.convertAndSend("/topic/user-" + cp.getUserId(),
                            dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));
                }
            } catch (Exception e) {
                log.warn("Could not send universal user notification: {}", e.getMessage());
            }
        }

        return savedAssignment;
    }

    public Rating submitRating(Long assignmentId, Rating rating) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        rating.setAssignmentId(assignmentId);
        return ratingRepository.save(rating);
    }

    public ServiceAssignment getAssignmentById(Long id) {
        return serviceAssignmentRepository.findById(id).orElse(null);
    }

    public ServiceAssignment getAssignmentByRequestId(Long requestId) {
        return serviceAssignmentRepository.findByRequestId(requestId).stream().findFirst().orElse(null);
    }

    public com.lifesamadhan.api.dto.CustomerProfileResponseDTO getCustomerProfileData(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
                .orElseGet(
                        () -> customerProfileRepository.save(CustomerProfile.builder().userId(user.getId()).build()));

        return dtoMapper.toCustomerProfileResponseDTO(user, profile);
    }

    public com.lifesamadhan.api.dto.CustomerProfileResponseDTO updateCustomerProfileData(String username,
            Map<String, Object> profileData) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (profileData.containsKey("name")) {
            user.setName((String) profileData.get("name"));
        }
        if (profileData.containsKey("mobile")) {
            user.setMobile((String) profileData.get("mobile"));
        }
        userRepository.save(user);

        CustomerProfile profile = customerProfileRepository.findByUserId(user.getId())
                .orElse(CustomerProfile.builder().userId(user.getId()).build());

        if (profileData.containsKey("address")) {
            profile.setAddress((String) profileData.get("address"));
        }
        if (profileData.containsKey("locationId")) {
            Object locId = profileData.get("locationId");
            if (locId != null) {
                Long lid = null;
                if (locId instanceof Long) {
                    lid = (Long) locId;
                } else if (locId instanceof Integer) {
                    lid = ((Integer) locId).longValue();
                } else if (locId instanceof String) {
                    try {
                        lid = Long.parseLong((String) locId);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid locationId format: {}", locId);
                    }
                }
                if (lid != null && lid > 0) {
                    profile.setLocationId(lid);
                }
            }
        }
        customerProfileRepository.save(profile);

        return getCustomerProfileData(username);
    }

    public List<com.lifesamadhan.api.dto.ProviderDTO> searchProviders(Long categoryId, String city) {
        log.info("=== PROVIDER SEARCH START ===");
        log.info("Params: CategoryID={}, City={}", categoryId, city);

        if (categoryId == null || city == null || city.trim().isEmpty()) {
            log.warn("Missing search parameters");
            return new ArrayList<>();
        }

        String cityTerm = city.trim();
        List<Location> locations = locationRepository.findAll().stream()
                .filter(l -> cityTerm.equalsIgnoreCase(l.getDistrict()))
                .collect(Collectors.toList());

        log.info("Found {} location records matching city '{}'", locations.size(), cityTerm);

        if (locations.isEmpty()) {
            log.warn("No locations found for city: {}", cityTerm);
            return new ArrayList<>();
        }

        List<Long> locationIds = locations.stream().map(Location::getId).collect(Collectors.toList());
        log.info("Location IDs: {}", locationIds);

        List<ProviderLocation> providerLocations = new ArrayList<>();
        for (Long locId : locationIds) {
            providerLocations.addAll(providerLocationRepository.findByLocationId(locId));
        }
        log.info("Total provider-location mappings found (any status): {}", providerLocations.size());

        Set<Long> providerIdsInLocation = providerLocations.stream()
                .filter(pl -> pl.getStatus() != null &&
                        (pl.getStatus().equalsIgnoreCase("APPROVED") ||
                                pl.getStatus().equalsIgnoreCase("ACTIVE") ||
                                pl.getStatus().equalsIgnoreCase("PENDING")))
                .map(ProviderLocation::getProviderId)
                .collect(Collectors.toSet());

        log.info("Unique provider IDs in these locations (all statuses): {}", providerIdsInLocation.size());

        if (providerIdsInLocation.isEmpty()) {
            return new ArrayList<>();
        }

        List<ServiceProvider> providers = serviceProviderRepository.findAllById(providerIdsInLocation).stream()
                .filter(sp -> categoryId.equals(sp.getServiceCategoryId()) && Boolean.TRUE.equals(sp.getVerified()))
                .collect(Collectors.toList());

        log.info("Providers matching Category ID {} in those locations: {}", categoryId, providers.size());

        List<com.lifesamadhan.api.dto.ProviderDTO> result = new ArrayList<>();
        for (ServiceProvider sp : providers) {
            User user = userRepository.findById(sp.getUserId()).orElse(null);
            if (user == null) {
                log.warn("User record missing for Provider ID: {}", sp.getId());
                continue;
            }

            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                log.info("Skipping provider {} because user status is {}", user.getName(), user.getStatus());
                continue;
            }

            Double avgRating = ratingRepository.getAverageRatingForProvider(sp.getId());
            Long reviewCount = ratingRepository.getReviewCountForProvider(sp.getId());

            result.add(com.lifesamadhan.api.dto.ProviderDTO.builder()
                    .id(sp.getId())
                    .providerId(sp.getId())
                    .name(user.getName())
                    .hourlyRate(sp.getHourlyRate())
                    .rating(avgRating != null ? avgRating : 0.0)
                    .completedJobs(sp.getCompletedJobsCount())
                    .reviewCount(reviewCount != null ? reviewCount.intValue() : 0)
                    .providerType(sp.getProviderType())
                    .city(cityTerm)
                    .build());
        }

        log.info("Returning {} providers for display", result.size());
        log.info("=== PROVIDER SEARCH END ===");
        return result;
    }

    public List<Location> getAllActiveLocations() {
        return locationRepository.findByStatus("ACTIVE");
    }

}