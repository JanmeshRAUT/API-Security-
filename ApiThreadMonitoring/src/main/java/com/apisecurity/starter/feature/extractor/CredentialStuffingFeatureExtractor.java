package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;
import com.apisecurity.starter.feature.state.BehaviorStateStore;
import org.springframework.util.AntPathMatcher;

import java.util.List;

public class CredentialStuffingFeatureExtractor implements SecurityFeatureExtractor {

    private final BehaviorStateStore stateStore;
    private final ApiSecurityProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public CredentialStuffingFeatureExtractor(BehaviorStateStore stateStore, ApiSecurityProperties properties) {
        this.stateStore = stateStore;
        this.properties = properties;
    }

    @Override
    public void extract(ApiSecurityEvent event, SecurityFeatureSet.SecurityFeatureSetBuilder featureSetBuilder) {
        String endpoint = event.getRequest().getEndpoint();
        List<String> loginPatterns = properties.getDetection().getAuthentication().getEndpointPatterns();
        
        boolean isLogin = loginPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, endpoint));
        if (!isLogin) {
            return; // Not a login request, skip
        }

        String sourceIp = event.getRequest().getSourceIp();
        String identityKey = "login_ip_" + sourceIp;
        
        // Extract behavioral features
        int totalRequests = stateStore.incrementAndGetRequestCount(identityKey);
        
        boolean isFailed = event.getResponse().getStatusCode() >= 400;
        int failedRequests = isFailed ? stateStore.incrementAndGetFailedCount(identityKey) : stateStore.getFailedCount(identityKey);
        int successRequests = !isFailed ? stateStore.incrementAndGetSuccessCount(identityKey) : stateStore.getSuccessCount(identityKey);
        
        double failureRatio = totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0;

        int uniqueUsers = 0;
        if (event.getIdentity().getUserId() != null) {
            uniqueUsers = stateStore.addToUniqueSetAndGetSize("login_users_from_" + sourceIp, event.getIdentity().getUserId());
        } else if (event.getIdentity().getTargetAccountId() != null) {
            uniqueUsers = stateStore.addToUniqueSetAndGetSize("login_users_from_" + sourceIp, event.getIdentity().getTargetAccountId());
        } else {
            uniqueUsers = stateStore.getUniqueSetSize("login_users_from_" + sourceIp);
        }

        int uniqueSourceIps = stateStore.addToUniqueSetAndGetSize("login_ips", sourceIp);

        SecurityFeatureSet currentSet = featureSetBuilder.build();
        SecurityFeatureSet.BehaviorFeatures current = currentSet.getBehaviorFeatures();

        SecurityFeatureSet.BehaviorFeatures.BehaviorFeaturesBuilder behaviorBuilder = current != null
                ? SecurityFeatureSet.BehaviorFeatures.builder()
                        .uniqueObjectIds(current.getUniqueObjectIds())
                        .objectsAccessedPerUser(current.getObjectsAccessedPerUser())
                        .sequentialObjectAccess(current.isSequentialObjectAccess())
                        .objectAccessFrequency(current.getObjectAccessFrequency())
                        .differentResourcesAccessed(current.getDifferentResourcesAccessed())
                        .accessedObjectIds(current.getAccessedObjectIds())
                        .resourceOwnerId(current.getResourceOwnerId())
                        .requestedObjectId(current.getRequestedObjectId())
                        .authorizationDecision(current.getAuthorizationDecision())
                : SecurityFeatureSet.BehaviorFeatures.builder();

        behaviorBuilder.requestFrequency(totalRequests)
                .failedRequestCount(failedRequests)
                .successfulRequestCount(successRequests)
                .failureRatio(failureRatio)
                .uniqueUsers(uniqueUsers)
                .uniqueSourceIps(uniqueSourceIps);
                
        featureSetBuilder.behaviorFeatures(behaviorBuilder.build());
    }
}
