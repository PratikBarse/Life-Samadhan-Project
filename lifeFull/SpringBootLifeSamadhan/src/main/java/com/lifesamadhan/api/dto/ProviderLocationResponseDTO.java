package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderLocationResponseDTO {
    private Long id;
    private Long providerId;
    private Long locationId;
    private String status;
    private String providerName;
    private String locationDetails;
}