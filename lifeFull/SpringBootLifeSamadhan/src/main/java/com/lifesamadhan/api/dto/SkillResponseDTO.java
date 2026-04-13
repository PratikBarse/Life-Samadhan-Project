package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponseDTO {
    private Long id;
    private Long providerId;
    private Long serviceId;
    private String status;
    private String serviceName;
    private String providerName;
}