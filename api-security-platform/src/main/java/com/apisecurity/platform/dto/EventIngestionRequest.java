package com.apisecurity.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventIngestionRequest {

    private String eventId;

    @NotBlank(message = "Application ID is required")
    private String applicationId;

    private String timestamp;

    // Flat convenience fields for simple clients/middlewares
    private String method;
    private String endpoint;
    private Integer statusCode;
    private Long responseTimeMs;
    private Integer requestSize;
    private Integer responseSize;
    private String clientIp;
    private String sourceIp;
    private String userAgent;
    private String userId;
    private Boolean authenticated;

    private RequestFeatures requestFeatures;
    private IdentityFeatures identityFeatures;
    private NetworkFeatures networkFeatures;
    private BehaviorFeatures behaviorFeatures;

    // Starter compatibility models
    private StarterRequest request;
    private StarterResponse response;
    private StarterIdentity identity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarterRequest {
        private String method;
        private String endpoint;
        private String rawUri;
        private String sourceIp;
        private String userAgent;
        private Integer requestSize;
        private Integer pathDepth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarterResponse {
        private int statusCode;
        private long responseTimeMs;
        private Integer responseSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StarterIdentity {
        private boolean authenticated;
        private String userId;
        private String targetAccountId;
        private String sessionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestFeatures {
        private String method;
        private String endpoint;
        private int statusCode;
        private long responseTimeMs;
        private Integer requestSize;
        private Integer responseSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentityFeatures {
        private boolean authenticated;
        private String userId;
        private String targetAccountId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkFeatures {
        private String sourceIp;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorFeatures {
        private int requestFrequency;
        private int failedRequestCount;
        private int successfulRequestCount;
        private Double failureRatio;
        private int uniqueUsers;
        private int uniqueSourceIps;

        private int uniqueObjectIds;
        private double objectsAccessedPerUser;
        private boolean sequentialObjectAccess;
        private double objectAccessFrequency;
        private int differentResourcesAccessed;
        private List<String> accessedObjectIds;

        private String resourceOwnerId;
        private String requestedObjectId;
        private String authorizationDecision;
    }
}
