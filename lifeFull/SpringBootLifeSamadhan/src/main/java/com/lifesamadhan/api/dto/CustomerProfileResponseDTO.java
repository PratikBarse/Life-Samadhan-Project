package com.lifesamadhan.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponseDTO {
    private UserResponseDTO user;
    private Long locationId;
    private String address;
    private String district;
    private String state;
    private String pincode;
}
