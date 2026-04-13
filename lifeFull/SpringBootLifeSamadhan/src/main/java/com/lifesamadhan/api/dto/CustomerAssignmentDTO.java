package com.lifesamadhan.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAssignmentDTO {
    
    private Long id;
    private Long userId;
    private Long locationId;
    private String address;
    private UserAssignmentDTO user;
}
