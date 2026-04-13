package com.lifesamadhan.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.extern.slf4j.Slf4j;

import com.lifesamadhan.api.model.ServiceAssignment;
import com.lifesamadhan.api.model.ServiceRequest;
import com.lifesamadhan.api.model.ServiceProvider;
import com.lifesamadhan.api.model.User;
import com.lifesamadhan.api.model.ProviderLocation;
import com.lifesamadhan.api.model.Location;
import com.lifesamadhan.api.repository.ServiceAssignmentRepository;
import com.lifesamadhan.api.repository.ServiceProviderRepository;
import com.lifesamadhan.api.repository.UserRepository;
import com.lifesamadhan.api.repository.ProviderLocationRepository;
import com.lifesamadhan.api.repository.LocationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ServiceAssignmentRepository serviceAssignmentRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final UserRepository userRepository;
    private final ProviderLocationRepository providerLocationRepository;
    private final LocationRepository locationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.lifesamadhan.api.repository.ProviderSkillRepository providerSkillRepository;
    private final com.lifesamadhan.api.dto.DTOMapper dtoMapper;
    private final EmailService emailService;
    private final com.lifesamadhan.api.repository.RatingRepository ratingRepository;
    private final com.lifesamadhan.api.repository.ServiceRequestRepository serviceRequestRepository;
    private final com.lifesamadhan.api.repository.CustomerProfileRepository customerProfileRepository;

    private void notifyCustomer(ServiceAssignment assignment) {
        Long customerId = null;
        if (assignment.getRequest() != null) {
            customerId = assignment.getRequest().getCustomerId();
        } else if (assignment.getRequestId() != null) {
            // Fallback: fetch request if not loaded
            customerId = serviceRequestRepository.findById(assignment.getRequestId())
                    .map(ServiceRequest::getCustomerId)
                    .orElse(null);
        }

        if (customerId == null) {
            log.warn("Cannot notify customer: Request/CustomerId not found for assignment {}", assignment.getId());
            return;
        }

        Object dto = dtoMapper.toServiceAssignmentResponseDTO(assignment);
        log.info("Sending Real-time Notification for Assignment ID: {}", assignment.getId());
        log.info(" - To Global: /topic/notifications");
        log.info(" - To Customer Profile: /topic/customer-{}", customerId);

        // 1. Global Notification (Fail-safe for frontend)
        messagingTemplate.convertAndSend("/topic/notifications", dto);

        // 2. Notify by Customer Profile ID
        messagingTemplate.convertAndSend("/topic/customer-" + customerId, dto);

        // 3. Notify by universal User ID
        try {
            final Long finalId = customerId;
            customerProfileRepository.findById(finalId).ifPresent(cp -> {
                if (cp.getUserId() != null) {
                    log.info(" - To User ID: /topic/user-{}", cp.getUserId());
                    messagingTemplate.convertAndSend("/topic/user-" + cp.getUserId(), dto);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to send universal notification: {}", e.getMessage());
        }
    }

    @Transactional
    public void updateProviderAvailability(String username, boolean isAvailable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ServiceProvider provider = serviceProviderRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        provider.setAvailability(isAvailable ? "AVAILABLE" : "OFFLINE");
        serviceProviderRepository.save(provider);
    }

    public List<ServiceAssignment> getAssignmentsByProviderId(Long providerId) {
        return serviceAssignmentRepository.findByProviderId(providerId);
    }

    public List<ServiceAssignment> getAssignmentsByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ServiceProvider provider = serviceProviderRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        return serviceAssignmentRepository.findByProviderId(provider.getId());
    }

    public List<com.lifesamadhan.api.model.ProviderSkill> getSkillsByProviderId(Long providerId) {
        return providerSkillRepository.findByProviderId(providerId);
    }

    @Transactional
    public ServiceAssignment acceptAssignment(Long assignmentId) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!"ASSIGNED".equals(assignment.getStatus())) {
            throw new RuntimeException("Request is no longer in ASSIGNED state");
        }

        assignment.setStatus("ACCEPTED");
        assignment.setAcceptedAt(java.time.LocalDateTime.now());

        if (assignment.getOtp() == null) {
            String otp = String.format("%04d", new java.util.Random().nextInt(10000));
            assignment.setOtp(otp);
        }

        ServiceAssignment savedAssignment = serviceAssignmentRepository.save(assignment);

        messagingTemplate.convertAndSend("/topic/request-" + assignment.getRequestId(),
                dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));

        if (assignment.getRequest() != null) {
            notifyCustomer(savedAssignment);
        }

        try {
            if (assignment.getRequest() != null && assignment.getRequest().getCustomer() != null
                    && assignment.getRequest().getCustomer().getUser() != null) {
                String customerEmail = assignment.getRequest().getCustomer().getUser().getEmail();
                String providerName = "The Provider";
                if (assignment.getProvider() != null && assignment.getProvider().getUser() != null) {
                    providerName = assignment.getProvider().getUser().getName();
                }
                String serviceName = assignment.getRequest().getService() != null
                        ? assignment.getRequest().getService().getName()
                        : "Service";

                emailService.sendOtpEmail(customerEmail, assignment.getOtp(), providerName, serviceName);
            }
        } catch (Exception e) {
            System.err.println("Failed to trigger email: " + e.getMessage());
        }

        return savedAssignment;
    }

    @Transactional
    public ServiceAssignment rejectAssignment(Long assignmentId) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!"ASSIGNED".equals(assignment.getStatus())) {
            throw new RuntimeException("Request is no longer in ASSIGNED state");
        }

        assignment.setStatus("REJECTED");

        // Update provider stats
        ServiceProvider provider = serviceProviderRepository.findById(assignment.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        Integer currentRejected = provider.getRejectedJobsCount();
        provider.setRejectedJobsCount(currentRejected != null ? currentRejected + 1 : 1);
        serviceProviderRepository.save(provider);

        // Cancel parent request
        if (assignment.getRequest() != null) {
            ServiceRequest req = assignment.getRequest();
            req.setStatus("CANCELLED");
            serviceRequestRepository.save(req);
        }

        ServiceAssignment savedAssignment = serviceAssignmentRepository.save(assignment);
        Object dto = dtoMapper.toServiceAssignmentResponseDTO(savedAssignment);

        // 1. Notify Provider (Self) - ensures other tabs update
        log.info("Notifying Provider {} about rejection of assignment {}", provider.getId(), assignmentId);
        messagingTemplate.convertAndSend("/topic/provider-" + provider.getId(), dto);

        // 2. Notify Customer
        if (assignment.getRequest() != null) {
            log.info("Notifying Customer {} about rejection of Request {}", assignment.getRequest().getCustomerId(),
                    assignment.getRequestId());
            notifyCustomer(savedAssignment);
        }

        return savedAssignment;
    }

    @Transactional
    public ServiceAssignment startService(Long assignmentId, String otp) {
        ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!"ACCEPTED".equals(assignment.getStatus())) {
            throw new RuntimeException("Service not yet accepted");
        }

        if (assignment.getOtp() == null || !assignment.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        assignment.setStatus("STARTED");
        assignment.setStartedAt(java.time.LocalDateTime.now());
        ServiceAssignment savedAssignment = serviceAssignmentRepository.save(assignment);

        if (assignment.getRequest() != null) {
            ServiceRequest req = assignment.getRequest();
            req.setStatus("IN_PROGRESS");
            serviceRequestRepository.save(req);
        }

        messagingTemplate.convertAndSend("/topic/request-" + assignment.getRequestId(),
                dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));

        if (assignment.getRequest() != null) {
            notifyCustomer(savedAssignment);
        }

        return savedAssignment;
    }

    @Transactional
    public ServiceAssignment completeService(Long assignmentId) {
        if (assignmentId == null || assignmentId <= 0) {
            throw new IllegalArgumentException("Invalid assignment ID");
        }

        try {
            ServiceAssignment assignment = serviceAssignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            if (!"STARTED".equals(assignment.getStatus())) {
                throw new RuntimeException("Service not started yet");
            }

            assignment.setStatus("COMPLETED");
            assignment.setCompletedAt(java.time.LocalDateTime.now());

            ServiceProvider provider = serviceProviderRepository.findById(assignment.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            Integer currentCompleted = provider.getCompletedJobsCount();
            provider.setCompletedJobsCount(currentCompleted != null ? currentCompleted + 1 : 1);
            serviceProviderRepository.save(provider);

            if (assignment.getRequest() != null) {
                ServiceRequest req = assignment.getRequest();
                req.setStatus("COMPLETED");
                req.setCompletionDate(java.time.LocalDateTime.now());

                if (assignment.getStartedAt() != null) {
                    java.time.Duration duration = java.time.Duration.between(assignment.getStartedAt(),
                            assignment.getCompletedAt());
                    double hours = Math.max(1.0, Math.round((duration.toMinutes() / 60.0) * 100.0) / 100.0);

                    if (provider.getHourlyRate() != null) {
                        req.setAmount(hours * provider.getHourlyRate());
                    }
                }

                serviceRequestRepository.save(req);
            }

            ServiceAssignment savedAssignment = serviceAssignmentRepository.save(assignment);

            messagingTemplate.convertAndSend("/topic/request-" + assignment.getRequestId(),
                    dtoMapper.toServiceAssignmentResponseDTO(savedAssignment));

            // Also notify Customer directly
            if (assignment.getRequest() != null) {
                notifyCustomer(savedAssignment);
            }

            return savedAssignment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to complete service: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ProviderLocation setProviderLocation(String username, Long locationId) {
        if (locationId == null || locationId <= 0) {
            throw new IllegalArgumentException("Invalid location ID");
        }

        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ServiceProvider provider = serviceProviderRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            List<ProviderLocation> existingLocations = providerLocationRepository.findByProviderId(provider.getId());

            ProviderLocation providerLocation;
            if (!existingLocations.isEmpty()) {

                providerLocation = existingLocations.get(0);
                providerLocation.setLocationId(locationId);
                providerLocation.setStatus("ACTIVE");
            } else {

                providerLocation = ProviderLocation.builder()
                        .providerId(provider.getId())
                        .locationId(locationId)
                        .status("ACTIVE")
                        .build();
            }

            return providerLocationRepository.save(providerLocation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set provider location: " + e.getMessage(), e);
        }
    }

    public List<Location> getAllActiveLocations() {
        return locationRepository.findByStatus("ACTIVE");
    }

    public String getProviderLocation(String username) {
        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ServiceProvider provider = serviceProviderRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Provider not found"));

            List<ProviderLocation> providerLocations = providerLocationRepository.findByProviderId(provider.getId());
            if (!providerLocations.isEmpty()) {

                ProviderLocation activeLocation = providerLocations.stream()
                        .filter(pl -> "ACTIVE".equals(pl.getStatus()))
                        .findFirst()
                        .orElseGet(() -> providerLocations.stream()
                                .filter(pl -> "APPROVED".equals(pl.getStatus()))
                                .findFirst()
                                .orElse(providerLocations.get(0)));

                Location location = locationRepository.findById(activeLocation.getLocationId())
                        .orElse(null);

                if (location != null) {
                    return location.getDistrict() + ", " + location.getState();
                }
            }
            return "Location not set";
        } catch (Exception e) {
            return "Location not set";
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ServiceProvider getProviderByUserId(Long userId) {
        return serviceProviderRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
    }

    public java.util.Map<String, Object> getProviderEarnings(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ServiceProvider provider = serviceProviderRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        List<com.lifesamadhan.api.model.ServiceRequest> completedRequests = serviceRequestRepository
                .findAll()

                .stream()
                .filter(r -> r.getProviderId() != null &&
                        r.getProviderId().equals(provider.getId()) &&
                        ("COMPLETED".equals(r.getStatus()) || "PAID".equals(r.getStatus())))
                .collect(java.util.stream.Collectors.toList());

        double totalEarnings = completedRequests.stream()
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0.0)
                .sum();

        List<Long> requestIds = completedRequests.stream().map(com.lifesamadhan.api.model.ServiceRequest::getId)
                .collect(java.util.stream.Collectors.toList());
        List<ServiceAssignment> assignments = serviceAssignmentRepository.findAll().stream()
                .filter(a -> requestIds.contains(a.getRequestId()) && "COMPLETED".equals(a.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        double totalHrs = 0;
        for (ServiceAssignment a : assignments) {
            if (a.getStartedAt() != null && a.getCompletedAt() != null) {
                java.time.Duration d = java.time.Duration.between(a.getStartedAt(), a.getCompletedAt());
                totalHrs += (d.toMinutes() / 60.0);
            }
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalEarnings", totalEarnings);
        response.put("totalJobs", completedRequests.size());
        response.put("totalHours", Math.round(totalHrs * 10.0) / 10.0);

        Double avgRating = ratingRepository.getAverageRatingForProvider(provider.getId());
        Long reviewCount = ratingRepository.getReviewCountForProvider(provider.getId());

        response.put("averageRating", avgRating != null ? avgRating : 0.0);
        response.put("ratingCount", reviewCount != null ? reviewCount : 0);

        java.util.List<java.util.Map<String, Object>> history = completedRequests.stream().map(r -> {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", r.getId());
            item.put("completionDate", r.getCompletionDate() != null ? r.getCompletionDate() : r.getCreatedAt());
            item.put("service", (r.getService() != null) ? r.getService().getName() : "Service");
            item.put("amount", r.getAmount());
            return item;
        }).collect(java.util.stream.Collectors.toList());

        response.put("history", history);
        return response;
    }
}