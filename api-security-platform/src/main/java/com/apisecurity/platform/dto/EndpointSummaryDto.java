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
public class EndpointSummaryDto {
    private String endpoint;
    private String applicationId;
    private List<String> methods;
    private long totalRequests;
    private double avgLatencyMs;
    private long minLatencyMs;
    private long maxLatencyMs;
    private long errorCount;
    private double errorRate;
    private String lastSeenAt;
}
