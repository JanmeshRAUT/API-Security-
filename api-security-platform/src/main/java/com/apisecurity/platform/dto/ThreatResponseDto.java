package com.apisecurity.platform.dto;

import com.apisecurity.platform.domain.threat.RecommendedAction;
import com.apisecurity.platform.domain.threat.Severity;
import com.apisecurity.platform.domain.threat.ThreatStatus;
import com.apisecurity.platform.domain.threat.ThreatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatResponseDto {

    private String id;
    private String eventId;
    private String applicationId;
    private ThreatType threatType;
    private double confidence;
    private double riskScore;
    private Severity severity;
    private RecommendedAction recommendedAction;
    private ThreatStatus status;
    private List<String> reasonCodes;
    private LocalDateTime detectedAt;
    private String endpoint;
    private String sourceIpHash;
    private String userHash;
    private String httpMethod;
    private int statusCode;
}
