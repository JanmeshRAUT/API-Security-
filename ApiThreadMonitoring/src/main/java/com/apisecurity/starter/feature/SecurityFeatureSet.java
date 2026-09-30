package com.apisecurity.starter.feature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityFeatureSet {
    
    private String eventId;
    private String applicationId;
    private String timestamp;

    private RequestFeatures requestFeatures;
    private IdentityFeatures identityFeatures;
    private NetworkFeatures networkFeatures;
    private BehaviorFeatures behaviorFeatures;

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
        private Integer queryParamsCount;
        private Integer pathDepth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentityFeatures {
        private boolean authenticated;
        private String userId;
        private String targetAccountId;
        private String sessionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkFeatures {
        private String sourceIp;
        private String clientIp;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviorFeatures {
        // Credential Stuffing & Behavioral Features
        private int requestFrequency;
        private int failedRequestCount;
        private int successfulRequestCount;
        private Double failureRatio;
        private int uniqueUsers;
        private int uniqueSourceIps;
        private Integer ipAccountCombinations;
        private Integer timeWindowSeconds;
        
        // BOLA / ID Enumeration Features
        private int uniqueObjectIds;
        private double objectsAccessedPerUser;
        private boolean sequentialObjectAccess;
        private double objectAccessFrequency;
        private int differentResourcesAccessed;
        private List<String> accessedObjectIds;

        // Authorization Context
        private String resourceOwnerId;
        private String requestedObjectId;
        private String authorizationDecision;
    }
}
