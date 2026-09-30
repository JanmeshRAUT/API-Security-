package com.apisecurity.platform.dto.realtime;

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
public class RealtimeThreatDto {

    private String id;
    private String threatId;
    private String eventId;
    private String applicationId;
    private ThreatType threatType;
    private double riskScore;
    private double confidence;
    private Severity severity;
    private String endpoint;
    private LocalDateTime detectedAt;
    private List<String> reasonCodes;
    private RecommendedAction recommendedAction;
    private ThreatStatus status;
}
