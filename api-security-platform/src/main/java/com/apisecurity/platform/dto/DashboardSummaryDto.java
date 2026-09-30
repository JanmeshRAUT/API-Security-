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
public class DashboardSummaryDto {

    private long totalApplications;
    private long totalEvents;
    private long totalThreats;
    private long criticalThreats;
    private long highThreats;
    private long credentialStuffingThreats;
    private long bolaThreats;
    private long idEnumerationThreats;
    private Map<String, Long> threatsByType;
    private Map<String, Long> threatsBySeverity;
}
