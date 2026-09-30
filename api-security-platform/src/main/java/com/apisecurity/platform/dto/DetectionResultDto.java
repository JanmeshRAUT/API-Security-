package com.apisecurity.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResultDto {

    private String eventId;
    private boolean detected;
    private String threatType;
    private double confidence;
    private double riskScore;
    private String severity;
    private String recommendedAction;
    private List<String> reasonCodes;
}
