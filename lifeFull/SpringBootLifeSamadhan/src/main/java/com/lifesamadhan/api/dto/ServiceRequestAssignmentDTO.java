package com.lifesamadhan.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestAssignmentDTO {
    
    private Long id;
    private Long customerId;
    private Long serviceId;
    private Long locationId;
    private String status;
    private LocalDateTime createdAt;
    private CustomerAssignmentDTO customer;
}
