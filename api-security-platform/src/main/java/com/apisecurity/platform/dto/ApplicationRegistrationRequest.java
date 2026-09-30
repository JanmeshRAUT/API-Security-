package com.apisecurity.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationRegistrationRequest {

    @NotBlank(message = "Application name is required")
    private String name;

    @NotBlank(message = "Application ID is required")
    private String applicationId;

    private String description;
}
