package com.lifesamadhan.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceDTO {
    @NotBlank(message = "Service name is required")
    @Size(min = 3, max = 150, message = "Service name must be between 3 and 150 characters")
    private String name;
    
    @NotNull(message = "CategoryId is required")
    @Min(value = 1, message = "CategoryId must be a valid value")
    private Long categoryId;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status;
}