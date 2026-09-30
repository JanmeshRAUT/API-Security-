package com.apisecurity.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRegistrationResponse {

    private String id;
    private String name;
    private String applicationId;
    private String apiKey; // Plaintext secret generated once
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
