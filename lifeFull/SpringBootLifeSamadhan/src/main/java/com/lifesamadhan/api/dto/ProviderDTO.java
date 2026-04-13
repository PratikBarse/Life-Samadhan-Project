package com.lifesamadhan.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDTO {
    private Long id;
    private Long providerId;
    private String name;
    private Double hourlyRate;
    private Double rating;
    private Integer completedJobs;
    private Integer reviewCount;
    private String providerType;
    private String city;
}
