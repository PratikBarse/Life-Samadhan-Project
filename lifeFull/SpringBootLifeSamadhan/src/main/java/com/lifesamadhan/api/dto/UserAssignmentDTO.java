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
public class UserAssignmentDTO {
    
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
