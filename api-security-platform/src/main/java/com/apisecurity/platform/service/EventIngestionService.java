package com.apisecurity.platform.service;

import com.apisecurity.platform.client.AiDetectionClient;
import com.apisecurity.platform.domain.application.ApplicationEntity;
import com.apisecurity.platform.domain.event.ProcessingStatus;
import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.domain.threat.*;
import com.apisecurity.platform.dto.DetectionResultDto;
import com.apisecurity.platform.dto.EventIngestionRequest;
import com.apisecurity.platform.dto.EventResponseDto;
import com.apisecurity.platform.exception.UnauthorizedException;
import com.apisecurity.platform.repository.SecurityEventRepository;
import com.apisecurity.platform.repository.ThreatRepository;
import com.apisecurity.platform.publisher.RealTimeSecurityPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.apisecurity.platform.mapper.SecurityEventMapper;

@Service
public class EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final ApplicationService applicationService;
    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;
    private final AiDetectionClient aiDetectionClient;
    private final RealTimeSecurityPublisher realTimePublisher;
    private final SecurityEventMapper securityEventMapper;
    private final ObjectMapper objectMapper;

    public EventIngestionService(
            ApplicationService applicationService,
            SecurityEventRepository eventRepository,
            ThreatRepository threatRepository,
            AiDetectionClient aiDetectionClient,
            RealTimeSecurityPublisher realTimePublisher,
            SecurityEventMapper securityEventMapper,
            ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
        this.aiDetectionClient = aiDetectionClient;
        this.realTimePublisher = realTimePublisher;
        this.securityEventMapper = securityEventMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DetectionResultDto ingestAndAnalyzeEvent(EventIngestionRequest request, String rawApiKey) {
        // 1. Authenticate or auto-register sending application
        String targetAppId = (request.getApplicationId() != null && !request.getApplicationId().isBlank())
                ? request.getApplicationId().trim()
                : "default-service";
        request.setApplicationId(targetAppId);

        ApplicationEntity app = applicationService.validateApiKey(rawApiKey)
                .orElseThrow(() -> new UnauthorizedException("Invalid or missing API key"));

        // Update application last seen timestamp
        applicationService.updateLastSeen(app.getApplicationId());

        // Extract metadata fields safely (supports flat fields, starter models, or nested feature blocks)
        String method = "GET";
        if (request.getMethod() != null) {
            method = request.getMethod();
        } else if (request.getRequest() != null && request.getRequest().getMethod() != null) {
            method = request.getRequest().getMethod();
        } else if (request.getRequestFeatures() != null && request.getRequestFeatures().getMethod() != null) {
            method = request.getRequestFeatures().getMethod();
        }

        String endpoint = "/";
        if (request.getEndpoint() != null) {
            endpoint = request.getEndpoint();
        } else if (request.getRequest() != null && request.getRequest().getEndpoint() != null) {
            endpoint = request.getRequest().getEndpoint();
        } else if (request.getRequestFeatures() != null && request.getRequestFeatures().getEndpoint() != null) {
            endpoint = request.getRequestFeatures().getEndpoint();
        }

        int statusCode = 200;
        if (request.getStatusCode() != null) {
            statusCode = request.getStatusCode();
        } else if (request.getResponse() != null && request.getResponse().getStatusCode() > 0) {
            statusCode = request.getResponse().getStatusCode();
        } else if (request.getRequestFeatures() != null && request.getRequestFeatures().getStatusCode() > 0) {
            statusCode = request.getRequestFeatures().getStatusCode();
        }

        long responseTimeMs = 0;
        if (request.getResponseTimeMs() != null) {
            responseTimeMs = request.getResponseTimeMs();
        } else if (request.getResponse() != null) {
            responseTimeMs = request.getResponse().getResponseTimeMs();
        } else if (request.getRequestFeatures() != null) {
            responseTimeMs = request.getRequestFeatures().getResponseTimeMs();
        }

        String sourceIp = "127.0.0.1";
        if (request.getClientIp() != null) {
            sourceIp = request.getClientIp();
        } else if (request.getSourceIp() != null) {
            sourceIp = request.getSourceIp();
        } else if (request.getRequest() != null && request.getRequest().getSourceIp() != null) {
            sourceIp = request.getRequest().getSourceIp();
        } else if (request.getNetworkFeatures() != null && request.getNetworkFeatures().getSourceIp() != null) {
            sourceIp = request.getNetworkFeatures().getSourceIp();
        }

        String userAgent = null;
        if (request.getUserAgent() != null) {
            userAgent = request.getUserAgent();
        } else if (request.getRequest() != null && request.getRequest().getUserAgent() != null) {
            userAgent = request.getRequest().getUserAgent();
        } else if (request.getNetworkFeatures() != null) {
            userAgent = request.getNetworkFeatures().getUserAgent();
        }

        Integer requestSize = null;
        if (request.getRequestSize() != null) {
            requestSize = request.getRequestSize();
        } else if (request.getRequest() != null && request.getRequest().getRequestSize() != null) {
            requestSize = request.getRequest().getRequestSize();
        } else if (request.getRequestFeatures() != null && request.getRequestFeatures().getRequestSize() != null) {
            requestSize = request.getRequestFeatures().getRequestSize();
        }

        Integer responseSize = null;
        if (request.getResponseSize() != null) {
            responseSize = request.getResponseSize();
        } else if (request.getResponse() != null && request.getResponse().getResponseSize() != null) {
            responseSize = request.getResponse().getResponseSize();
        } else if (request.getRequestFeatures() != null && request.getRequestFeatures().getResponseSize() != null) {
            responseSize = request.getRequestFeatures().getResponseSize();
        }

        boolean authenticated = false;
        String userId = null;
        if (request.getAuthenticated() != null) {
            authenticated = request.getAuthenticated();
        } else if (request.getIdentity() != null) {
            authenticated = request.getIdentity().isAuthenticated();
            userId = request.getIdentity().getUserId();
        } else if (request.getIdentityFeatures() != null) {
            authenticated = request.getIdentityFeatures().isAuthenticated();
            userId = request.getIdentityFeatures().getUserId();
        }
        if (request.getUserId() != null) {
            userId = request.getUserId();
        }

        String ipHash = hashIdentifier(sourceIp);
        String userHash = hashIdentifier(userId);

        if (request.getTimestamp() == null || request.getTimestamp().isBlank()) {
            request.setTimestamp(java.time.Instant.now().toString());
        }

        if (request.getEventId() == null || request.getEventId().isBlank()) {
            request.setEventId(java.util.UUID.randomUUID().toString());
        }

        LocalDateTime eventTime = parseTimestamp(request.getTimestamp());

        String rawJson = null;
        try {
            rawJson = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.warn("Failed to serialize event raw json: {}", e.getMessage());
        }

        // 2. Persist initial SecurityEventEntity in PENDING status
        SecurityEventEntity eventEntity = SecurityEventEntity.builder()
                .eventId(request.getEventId())
                .applicationId(app.getApplicationId())
                .timestamp(eventTime)
                .requestId(request.getEventId())
                .httpMethod(method.toUpperCase())
                .endpoint(endpoint)
                .sourceIpHash(ipHash)
                .clientIp(sourceIp)
                .userHash(userHash)
                .authenticated(authenticated)
                .statusCode(statusCode)
                .responseTimeMs(responseTimeMs)
                .userAgent(userAgent)
                .requestSize(requestSize)
                .responseSize(responseSize)
                .processingStatus(ProcessingStatus.PENDING)
                .rawFeaturesJson(rawJson)
                .build();

        eventEntity = eventRepository.save(eventEntity);
        
        // Broadcast real-time security event over WebSockets
        realTimePublisher.publishEvent(eventEntity);

        // Ensure non-null feature blocks for AI detection model schema
        if (request.getRequestFeatures() == null) {
            request.setRequestFeatures(EventIngestionRequest.RequestFeatures.builder()
                    .method("GET").endpoint("/").statusCode(200).responseTimeMs(0).build());
        }
        if (request.getIdentityFeatures() == null) {
            request.setIdentityFeatures(EventIngestionRequest.IdentityFeatures.builder()
                    .authenticated(false).build());
        }
        if (request.getNetworkFeatures() == null) {
            request.setNetworkFeatures(EventIngestionRequest.NetworkFeatures.builder()
                    .sourceIp("0.0.0.0").build());
        }
        if (request.getBehaviorFeatures() == null) {
            request.setBehaviorFeatures(EventIngestionRequest.BehaviorFeatures.builder()
                    .requestFrequency(1).failedRequestCount(0).successfulRequestCount(1)
                    .failureRatio(0.0).uniqueUsers(1).uniqueSourceIps(1)
                    .uniqueObjectIds(0).objectsAccessedPerUser(0.0).sequentialObjectAccess(false)
                    .objectAccessFrequency(0.0).differentResourcesAccessed(1)
                    .accessedObjectIds(java.util.Collections.emptyList()).build());
        }

        // 3. Delegate to AI Detection Engine
        Optional<DetectionResultDto> aiResultOpt = aiDetectionClient.detectThreat(request);

        if (aiResultOpt.isPresent()) {
            DetectionResultDto aiResult = aiResultOpt.get();
            eventEntity.setProcessingStatus(ProcessingStatus.PROCESSED);
            eventRepository.save(eventEntity);

            // 4. Create ThreatEntity if threat is detected
            if (aiResult.isDetected()) {
                ThreatType threatType = parseThreatType(aiResult.getThreatType());
                Severity severity = parseSeverity(aiResult.getSeverity());
                RecommendedAction action = parseAction(aiResult.getRecommendedAction());

                String reasonCodesJson = null;
                try {
                    reasonCodesJson = objectMapper.writeValueAsString(aiResult.getReasonCodes());
                } catch (Exception e) {
                    reasonCodesJson = "[]";
                }

                ThreatEntity threat = ThreatEntity.builder()
                        .eventId(eventEntity.getEventId())
                        .applicationId(eventEntity.getApplicationId())
                        .threatType(threatType)
                        .confidence(aiResult.getConfidence())
                        .riskScore(aiResult.getRiskScore())
                        .severity(severity)
                        .recommendedAction(action)
                        .status(ThreatStatus.OPEN)
                        .reasonCodes(reasonCodesJson)
                        .detectedAt(eventTime)
                        .endpoint(endpoint)
                        .sourceIpHash(ipHash)
                        .userHash(userHash)
                        .httpMethod(method)
                        .statusCode(statusCode)
                        .build();

                ThreatEntity savedThreat = threatRepository.save(threat);
                log.info("Threat incident created for event [{}], type [{}]", request.getEventId(), threatType);

                // Broadcast real-time threat alert over WebSockets
                realTimePublisher.publishThreat(savedThreat);
            }

            return aiResult;
        } else {
            // AI Detection service was unavailable or bypassed -> mark event as PROCESSED
            eventEntity.setProcessingStatus(ProcessingStatus.PROCESSED);
            eventRepository.save(eventEntity);
            log.info("Event [{}] recorded successfully (AI detection bypassed/offline)", request.getEventId());

            return DetectionResultDto.builder()
                    .eventId(request.getEventId())
                    .detected(false)
                    .threatType(ThreatType.NONE.name())
                    .confidence(0.0)
                    .riskScore(0.0)
                    .severity(Severity.LOW.name())
                    .recommendedAction(RecommendedAction.ALLOW.name())
                    .reasonCodes(java.util.List.of("AI_DETECTION_SERVICE_UNAVAILABLE"))
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDto> getEvents(Pageable pageable) {
        return eventRepository.findAll(pageable).map(securityEventMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDto> getEvents(
            String applicationId,
            String httpMethod,
            String endpoint,
            Integer statusCode,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir != null && sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp").ascending()
                : Sort.by(sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<SecurityEventEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (applicationId != null && !applicationId.isBlank()) {
                predicates.add(cb.equal(root.get("applicationId"), applicationId.trim()));
            }
            if (httpMethod != null && !httpMethod.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("httpMethod")), httpMethod.trim().toUpperCase()));
            }
            if (endpoint != null && !endpoint.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("endpoint")), "%" + endpoint.trim().toLowerCase() + "%"));
            }
            if (statusCode != null) {
                predicates.add(cb.equal(root.get("statusCode"), statusCode));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return eventRepository.findAll(spec, pageable).map(securityEventMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseDto> getEventsByApplication(String applicationId, Pageable pageable) {
        return eventRepository.findByApplicationId(applicationId, pageable).map(securityEventMapper::toResponseDto);
    }

    private String hashIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 16);
        } catch (Exception e) {
            return "hashed";
        }
    }

    private LocalDateTime parseTimestamp(String ts) {
        if (ts == null || ts.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(ts, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private ThreatType parseThreatType(String val) {
        try { return ThreatType.valueOf(val.toUpperCase()); } catch (Exception e) { return ThreatType.NONE; }
    }

    private Severity parseSeverity(String val) {
        try { return Severity.valueOf(val.toUpperCase()); } catch (Exception e) { return Severity.LOW; }
    }

    private RecommendedAction parseAction(String val) {
        try { return RecommendedAction.valueOf(val.toUpperCase()); } catch (Exception e) { return RecommendedAction.ALLOW; }
    }
}
