package com.apisecurity.platform.dto;

import com.apisecurity.platform.domain.threat.ThreatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ThreatStatusUpdateRequest {

    @NotNull(message = "Threat status is required")
    private ThreatStatus status;
}
