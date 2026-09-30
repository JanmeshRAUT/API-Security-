package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CompositeFeatureExtractor implements SecurityFeatureExtractor {

    private final List<SecurityFeatureExtractor> extractors;
    private final ApiSecurityProperties properties;

    public CompositeFeatureExtractor(List<SecurityFeatureExtractor> extractors, ApiSecurityProperties properties) {
        this.extractors = extractors;
        this.properties = properties;
    }

    @Override
    public void extract(ApiSecurityEvent event, SecurityFeatureSet.SecurityFeatureSetBuilder featureSetBuilder) {
        
        // Base mapping
        featureSetBuilder.eventId(event.getEventId())
                .applicationId(event.getApplicationId())
                .timestamp(event.getTimestamp());

        // Basic Features Mapping
        featureSetBuilder.requestFeatures(SecurityFeatureSet.RequestFeatures.builder()
                .method(event.getRequest().getMethod())
                .endpoint(event.getRequest().getEndpoint())
                .statusCode(event.getResponse().getStatusCode())
                .responseTimeMs(event.getResponse().getResponseTimeMs())
                .requestSize(event.getRequest().getRequestSize())
                .responseSize(event.getResponse().getResponseSize())
                .queryParamsCount(event.getRequest().getQueryParameters() != null ? event.getRequest().getQueryParameters().size() : 0)
                .pathDepth(event.getRequest().getPathDepth())
                .build());

        String rawClientIp = event.getRequest().getSourceIp();
        String sourceIp = rawClientIp;
        String userId = event.getIdentity().getUserId();
        String targetAccountId = event.getIdentity().getTargetAccountId();

        if (properties.getDetection().getPrivacy().isHashIdentifiers()) {
            sourceIp = hash(sourceIp);
            if (userId != null) {
                userId = hash(userId);
            }
            if (targetAccountId != null) {
                targetAccountId = hash(targetAccountId);
            }
        }

        featureSetBuilder.identityFeatures(SecurityFeatureSet.IdentityFeatures.builder()
                .authenticated(event.getIdentity().isAuthenticated())
                .userId(userId)
                .targetAccountId(targetAccountId)
                .sessionId(event.getIdentity().getSessionId())
                .build());

        featureSetBuilder.networkFeatures(SecurityFeatureSet.NetworkFeatures.builder()
                .sourceIp(sourceIp)
                .clientIp(rawClientIp != null ? rawClientIp : "127.0.0.1")
                .userAgent(event.getRequest().getUserAgent())
                .build());

        // Initialize default behavioral features
        SecurityFeatureSet.BehaviorFeatures baseBehavior = SecurityFeatureSet.BehaviorFeatures.builder()
                .requestFrequency(1)
                .failedRequestCount(event.getResponse().getStatusCode() >= 400 ? 1 : 0)
                .successfulRequestCount(event.getResponse().getStatusCode() < 400 ? 1 : 0)
                .failureRatio(event.getResponse().getStatusCode() >= 400 ? 1.0 : 0.0)
                .uniqueUsers(1)
                .uniqueSourceIps(1)
                .uniqueObjectIds(0)
                .objectsAccessedPerUser(0.0)
                .sequentialObjectAccess(false)
                .objectAccessFrequency(0.0)
                .differentResourcesAccessed(1)
                .accessedObjectIds(java.util.Collections.emptyList())
                .build();
        featureSetBuilder.behaviorFeatures(baseBehavior);

        // Delegate to specific behavioral extractors
        for (SecurityFeatureExtractor extractor : extractors) {
            extractor.extract(event, featureSetBuilder);
        }
    }
    
    private String hash(String input) {
        if (input == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }
}
