package com.apisecurity.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMetricsDto {
    private long totalRequests;
    private long totalEndpoints;
    private long activeApplications;
    private double avgLatencyMs;
    private long errorCount;
    private double errorRate;
    private Map<String, Long> statusBreakdown;
    private Map<String, Long> methodBreakdown;
}
