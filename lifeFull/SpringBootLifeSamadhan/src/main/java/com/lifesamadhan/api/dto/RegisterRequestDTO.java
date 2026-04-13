package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {
    
    private String name;
    private String email;
    private String mobile;
    private String password;
    private String role; 

    
    private String address;

    
    private String providerType;
    private Double hourlyRate;
    private Long locationId;
    private Long serviceCategoryId;
    private String skills; 

    
    private String city;
    private String state;
}
