package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private String role;
    private String message;
    private String name;
    private String email;
    private String location;
    private Long userId;
    private Long providerId;
    private Long customerProfileId;
}