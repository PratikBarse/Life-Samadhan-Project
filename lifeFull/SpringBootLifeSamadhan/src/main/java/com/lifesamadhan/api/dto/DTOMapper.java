package com.lifesamadhan.api.dto;

import com.lifesamadhan.api.model.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DTOMapper {

    private final ModelMapper modelMapper;
    private final com.lifesamadhan.api.repository.PaymentRepository paymentRepository;
    private final com.lifesamadhan.api.repository.RatingRepository ratingRepository;

    
    public UserResponseDTO toUserResponseDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    
    public CategoryResponseDTO toCategoryResponseDTO(ServiceCategory category) {
        return modelMapper.map(category, CategoryResponseDTO.class);
    }

    public ServiceCategory toServiceCategory(CreateCategoryDTO dto) {
        return modelMapper.map(dto, ServiceCategory.class);
    }

    public void updateServiceCategory(ServiceCategory category, UpdateCategoryDTO dto) {
        modelMapper.map(dto, category);
    }

    
    public ServiceResponseDTO toServiceResponseDTO(Service service) {
        ServiceResponseDTO dto = modelMapper.map(service, ServiceResponseDTO.class);
        if (service.getCategory() != null) {
            dto.setCategoryName(service.getCategory().getName());
        }
        return dto;
    }

    public Service toService(CreateServiceDTO dto) {
        return modelMapper.map(dto, Service.class);
    }

    public void updateService(Service service, UpdateServiceDTO dto) {
        modelMapper.map(dto, service);
    }

    
    public LocationResponseDTO toLocationResponseDTO(Location location) {
        return modelMapper.map(location, LocationResponseDTO.class);
    }

    public Location toLocation(CreateLocationDTO dto) {
        return modelMapper.map(dto, Location.class);
    }

    public void updateLocation(Location location, UpdateLocationDTO dto) {
        modelMapper.map(dto, location);
    }

    
    public SkillResponseDTO toSkillResponseDTO(ProviderSkill skill) {
        SkillResponseDTO dto = modelMapper.map(skill, SkillResponseDTO.class);

        if (skill.getService() != null) {
            dto.setServiceName(skill.getService().getName());
        }

        if (skill.getProvider() != null && skill.getProvider().getUser() != null) {
            dto.setProviderName(skill.getProvider().getUser().getName());
        }

        return dto;
    }

    
    public ProviderLocationResponseDTO toProviderLocationResponseDTO(ProviderLocation providerLocation) {
        ProviderLocationResponseDTO dto = modelMapper.map(providerLocation, ProviderLocationResponseDTO.class);

        if (providerLocation.getProvider() != null && providerLocation.getProvider().getUser() != null) {
            dto.setProviderName(providerLocation.getProvider().getUser().getName());
        }

        if (providerLocation.getLocation() != null) {
            dto.setLocationDetails(providerLocation.getLocation().getDistrict() + ", " +
                    providerLocation.getLocation().getState());
        }

        return dto;
    }

    public ProviderLocation toProviderLocation(CreateProviderLocationDTO dto) {
        return modelMapper.map(dto, ProviderLocation.class);
    }

    public ProviderSkill toProviderSkill(CreateProviderSkillDTO dto) {
        return modelMapper.map(dto, ProviderSkill.class);
    }

    
    public java.util.Map<String, Object> toLegacyAssignmentMap(ServiceAssignment assignment) {
        ServiceAssignmentResponseDTO dto = toServiceAssignmentResponseDTO(assignment);

        java.util.Map<String, Object> map = new java.util.HashMap<>();

        
        map.put("assignment", dto);

        
        java.util.Map<String, Object> serviceMap = new java.util.HashMap<>();
        if (assignment.getRequest() != null && assignment.getRequest().getService() != null) {
            serviceMap.put("id", assignment.getRequest().getServiceId());
            serviceMap.put("name", assignment.getRequest().getService().getName());
        } else {
            serviceMap.put("name", dto.getServiceName());
        }
        map.put("service", serviceMap);

        
        java.util.Map<String, Object> providerMap = new java.util.HashMap<>();
        if (assignment.getProvider() != null) {
            providerMap.put("id", assignment.getProvider().getId());
            if (assignment.getProvider().getUser() != null) {
                providerMap.put("userName", assignment.getProvider().getUser().getName());
                providerMap.put("mobile", assignment.getProvider().getUser().getMobile());
            }
        } else {
            providerMap.put("userName", dto.getProviderName());
        }
        map.put("provider", providerMap);

        
        java.util.Map<String, Object> customerMap = new java.util.HashMap<>();
        if (assignment.getRequest() != null && assignment.getRequest().getCustomer() != null) {
            customerMap.put("id", assignment.getRequest().getCustomerId());
            if (assignment.getRequest().getCustomer().getUser() != null) {
                customerMap.put("name", assignment.getRequest().getCustomer().getUser().getName());
                customerMap.put("mobile", assignment.getRequest().getCustomer().getUser().getMobile());
            }
        }
        map.put("customer", customerMap);

        
        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        if (assignment.getRequest() != null) {
            requestMap.put("id", assignment.getRequest().getId());
            requestMap.put("serviceAddress", assignment.getRequest().getServiceAddress());
            requestMap.put("scheduledDate", assignment.getRequest().getScheduledDate());
            requestMap.put("amount", assignment.getRequest().getAmount());
            requestMap.put("paymentStatus", dto.getPaymentStatus());
        }
        map.put("request", requestMap);

        
        java.util.Map<String, Object> ratingMap = new java.util.HashMap<>();
        if (dto.getRatingStars() != null) {
            ratingMap.put("stars", dto.getRatingStars());
            ratingMap.put("comment", dto.getRatingFeedback());
        }
        map.put("rating", ratingMap.isEmpty() ? null : ratingMap);

        return map;
    }

    public ServiceAssignmentResponseDTO toServiceAssignmentResponseDTO(ServiceAssignment assignment) {
        ServiceAssignmentResponseDTO dto = ServiceAssignmentResponseDTO.builder()
                .id(assignment.getId())
                .requestId(assignment.getRequestId())
                .providerId(assignment.getProviderId())
                .status(assignment.getStatus())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .completedAt(assignment.getCompletedAt())
                .otp(assignment.getOtp())
                .build();

        
        if (assignment.getRequest() != null && assignment.getRequest().getService() != null) {
            dto.setServiceName(assignment.getRequest().getService().getName());
        }

        
        if (assignment.getProvider() != null) {
            if (assignment.getProvider().getUser() != null) {
                dto.setProviderName(assignment.getProvider().getUser().getName());
            }
            dto.setHourlyRate(assignment.getProvider().getHourlyRate());
        }

        if (assignment.getRequest() != null) {
            ServiceRequest request = assignment.getRequest();
            dto.setPaymentStatus(request.getPaymentStatus());
            dto.setPaidAmount(request.getPaidAmount());

            ServiceRequestAssignmentDTO requestDTO = ServiceRequestAssignmentDTO.builder()
                    .id(request.getId())
                    .customerId(request.getCustomerId())
                    .serviceId(request.getServiceId())
                    .locationId(request.getLocationId())
                    .status(request.getStatus())
                    .createdAt(request.getCreatedAt())
                    .build();

            if (request.getCustomer() != null) {
                CustomerProfile customer = request.getCustomer();
                CustomerAssignmentDTO customerDTO = CustomerAssignmentDTO.builder()
                        .id(customer.getId())
                        .userId(customer.getUserId())
                        .locationId(customer.getLocationId())
                        .address(customer.getAddress())
                        .build();

                if (customer.getUser() != null) {
                    User user = customer.getUser();
                    UserAssignmentDTO userDTO = UserAssignmentDTO.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .mobile(user.getMobile())
                            .role(user.getRole())
                            .status(user.getStatus())
                            .createdAt(user.getCreatedAt())
                            .build();
                    customerDTO.setUser(userDTO);
                }

                requestDTO.setCustomer(customerDTO);
            }

            dto.setRequest(requestDTO);
        }

        
        java.util.List<com.lifesamadhan.api.model.Payment> payments = paymentRepository
                .findByAssignmentId(assignment.getId());
        if (payments != null && !payments.isEmpty()) {
            
            com.lifesamadhan.api.model.Payment payment = payments.stream()
                    .filter(p -> "COMPLETED".equals(p.getPaymentStatus()))
                    .findFirst()
                    .orElse(payments.get(0));

            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setPaidAmount(payment.getAmount().doubleValue());
            if ("COMPLETED".equals(payment.getPaymentStatus())) {
                dto.setCompletedAt(payment.getCreatedAt());
            }
        }

        
        java.util.List<com.lifesamadhan.api.model.Rating> ratings = ratingRepository
                .findByAssignmentId(assignment.getId());
        if (ratings != null && !ratings.isEmpty()) {
            dto.setRatingStars(ratings.get(0).getStars());
            dto.setRatingFeedback(ratings.get(0).getFeedback());
        }

        return dto;
    }

    
    public ProviderResponseDTO toProviderResponseDTO(ServiceProvider provider) {
        ProviderResponseDTO dto = modelMapper.map(provider, ProviderResponseDTO.class);

        if (provider.getUser() != null) {
            dto.setUser(toUserResponseDTO(provider.getUser()));
        }

        if (provider.getCategory() != null) {
            dto.setCategoryName(provider.getCategory().getName());
        }

        if (provider.getLocations() != null && !provider.getLocations().isEmpty()) {
            Location loc = provider.getLocations().get(0).getLocation();
            if (loc != null) {
                dto.setCity(loc.getDistrict());
                dto.setState(loc.getState());
            }
        }

        return dto;
    }

    public CustomerProfileResponseDTO toCustomerProfileResponseDTO(com.lifesamadhan.api.model.User user,
            com.lifesamadhan.api.model.CustomerProfile profile) {
        CustomerProfileResponseDTO dto = CustomerProfileResponseDTO.builder()
                .user(toUserResponseDTO(user))
                .build();

        if (profile != null) {
            dto.setAddress(profile.getAddress());
            dto.setLocationId(profile.getLocationId());
            if (profile.getLocation() != null) {
                dto.setDistrict(profile.getLocation().getDistrict());
                dto.setState(profile.getLocation().getState());
                dto.setPincode(profile.getLocation().getPincode());
            }
        }

        return dto;
    }
}