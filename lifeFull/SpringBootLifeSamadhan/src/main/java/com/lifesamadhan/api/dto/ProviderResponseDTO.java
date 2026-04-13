package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponseDTO {
    private Long id;
    private UserResponseDTO user;
    private Boolean verified;
    private String availability;
    private Integer completedJobsCount;
    private Integer rejectedJobsCount;
    private Double hourlyRate;
    private String providerType;
    private Long serviceCategoryId;
    private String categoryName;
    private String city;
    private String state;
}
