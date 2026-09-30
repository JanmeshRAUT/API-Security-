package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.dto.ApiMetricsDto;
import com.apisecurity.platform.dto.EndpointSummaryDto;
import com.apisecurity.platform.repository.ApplicationRepository;
import com.apisecurity.platform.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EndpointCatalogService {

    private final SecurityEventRepository eventRepository;
    private final ApplicationRepository applicationRepository;

    public EndpointCatalogService(SecurityEventRepository eventRepository, ApplicationRepository applicationRepository) {
        this.eventRepository = eventRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<EndpointSummaryDto> getEndpoints(String applicationId) {
        List<SecurityEventEntity> events;
        if (applicationId != null && !applicationId.isBlank()) {
            events = eventRepository.findTop1000ByApplicationIdOrderByTimestampDesc(applicationId);
        } else {
            events = eventRepository.findTop1000ByOrderByTimestampDesc();
        }

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by endpoint + applicationId
        Map<String, List<SecurityEventEntity>> grouped = events.stream()
                .collect(Collectors.groupingBy(e -> e.getApplicationId() + ":::" + e.getEndpoint()));

        List<EndpointSummaryDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (Map.Entry<String, List<SecurityEventEntity>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split(":::");
            String app = parts[0];
            String endpoint = parts.length > 1 ? parts[1] : "/";
            List<SecurityEventEntity> list = entry.getValue();

            long total = list.size();
            List<String> methods = list.stream()
                    .map(SecurityEventEntity::getHttpMethod)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            long sumLatency = list.stream().mapToLong(SecurityEventEntity::getResponseTimeMs).sum();
            double avgLatency = total > 0 ? Math.round((double) sumLatency / total * 10.0) / 10.0 : 0.0;
            long minLatency = list.stream().mapToLong(SecurityEventEntity::getResponseTimeMs).min().orElse(0);
            long maxLatency = list.stream().mapToLong(SecurityEventEntity::getResponseTimeMs).max().orElse(0);

            long errors = list.stream().filter(e -> e.getStatusCode() >= 400).count();
            double errorRate = total > 0 ? Math.round(((double) errors / total) * 1000.0) / 10.0 : 0.0;

            String lastSeen = list.stream()
                    .map(SecurityEventEntity::getTimestamp)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .map(formatter::format)
                    .orElse("");

            result.add(EndpointSummaryDto.builder()
                    .applicationId(app)
                    .endpoint(endpoint)
                    .methods(methods)
                    .totalRequests(total)
                    .avgLatencyMs(avgLatency)
                    .minLatencyMs(minLatency)
                    .maxLatencyMs(maxLatency)
                    .errorCount(errors)
                    .errorRate(errorRate)
                    .lastSeenAt(lastSeen)
                    .build());
        }

        // Sort by total requests descending
        result.sort((a, b) -> Long.compare(b.getTotalRequests(), a.getTotalRequests()));
        return result;
    }

    @Transactional(readOnly = true)
    public ApiMetricsDto getMetrics(String applicationId) {
        List<SecurityEventEntity> events;
        if (applicationId != null && !applicationId.isBlank()) {
            events = eventRepository.findTop1000ByApplicationIdOrderByTimestampDesc(applicationId);
        } else {
            events = eventRepository.findTop1000ByOrderByTimestampDesc();
        }

        long total = events.size();
        Set<String> uniqueEndpoints = events.stream().map(SecurityEventEntity::getEndpoint).collect(Collectors.toSet());
        Set<String> uniqueApps = events.stream().map(SecurityEventEntity::getApplicationId).collect(Collectors.toSet());

        long sumLatency = events.stream().mapToLong(SecurityEventEntity::getResponseTimeMs).sum();
        double avgLatency = total > 0 ? Math.round((double) sumLatency / total * 10.0) / 10.0 : 0.0;
        long errors = events.stream().filter(e -> e.getStatusCode() >= 400).count();
        double errorRate = total > 0 ? Math.round(((double) errors / total) * 1000.0) / 10.0 : 0.0;

        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        statusBreakdown.put("2xx", events.stream().filter(e -> e.getStatusCode() >= 200 && e.getStatusCode() < 300).count());
        statusBreakdown.put("3xx", events.stream().filter(e -> e.getStatusCode() >= 300 && e.getStatusCode() < 400).count());
        statusBreakdown.put("4xx", events.stream().filter(e -> e.getStatusCode() >= 400 && e.getStatusCode() < 500).count());
        statusBreakdown.put("5xx", events.stream().filter(e -> e.getStatusCode() >= 500).count());

        Map<String, Long> methodBreakdown = events.stream()
                .filter(e -> e.getHttpMethod() != null)
                .collect(Collectors.groupingBy(SecurityEventEntity::getHttpMethod, Collectors.counting()));

        return ApiMetricsDto.builder()
                .totalRequests(total)
                .totalEndpoints(uniqueEndpoints.size())
                .activeApplications(uniqueApps.size() > 0 ? uniqueApps.size() : applicationRepository.count())
                .avgLatencyMs(avgLatency)
                .errorCount(errors)
                .errorRate(errorRate)
                .statusBreakdown(statusBreakdown)
                .methodBreakdown(methodBreakdown)
                .build();
    }
}
