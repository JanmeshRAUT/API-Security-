package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;
import com.apisecurity.starter.feature.state.BehaviorStateStore;

import java.util.*;
import java.util.regex.Pattern;

public class BolaFeatureExtractor implements SecurityFeatureExtractor {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    private final BehaviorStateStore stateStore;
    private final ApiSecurityProperties properties;

    public BolaFeatureExtractor(BehaviorStateStore stateStore, ApiSecurityProperties properties) {
        this.stateStore = stateStore;
        this.properties = properties;
    }

    @Override
    public void extract(ApiSecurityEvent event, SecurityFeatureSet.SecurityFeatureSetBuilder featureSetBuilder) {
        if (!properties.getDetection().getObjectAccess().isEnabled()) {
            return;
        }

        List<String> extractedObjectIds = extractObjectIds(event);
        if (extractedObjectIds.isEmpty()) {
            return;
        }

        String userId = event.getIdentity() != null ? event.getIdentity().getUserId() : null;
        String sourceIp = event.getRequest() != null ? event.getRequest().getSourceIp() : "unknown";
        String identity = userId != null ? userId : sourceIp;

        String primaryObjectId = extractedObjectIds.get(0);
        boolean sequentialAccess = false;

        for (String objId : extractedObjectIds) {
            boolean seq = stateStore.recordObjectAccessAndCheckSequential(identity, objId);
            if (seq) {
                sequentialAccess = true;
            }
        }

        int uniqueObjectIdsForUser = stateStore.getUniqueSetSize("object_ids_" + identity);
        int totalUniqueObjectIds = stateStore.getUniqueSetSize("all_object_ids");
        List<String> recentObjectIds = stateStore.getAccessedObjectIds(identity);

        int accessesForUser = stateStore.incrementAndGetRequestCount("object_req_" + identity);
        double objectAccessFrequency = (double) accessesForUser;

        SecurityFeatureSet currentSet = featureSetBuilder.build();
        SecurityFeatureSet.BehaviorFeatures current = currentSet.getBehaviorFeatures();

        int uniqueUsersCount = current != null && current.getUniqueUsers() > 0 ? current.getUniqueUsers() : 1;
        double objectsPerUser = (double) uniqueObjectIdsForUser / Math.max(1, uniqueUsersCount);

        int currentReqFreq = current != null ? current.getRequestFrequency() : 0;
        int reqFreq = Math.max(accessesForUser, currentReqFreq);

        SecurityFeatureSet.BehaviorFeatures.BehaviorFeaturesBuilder builder = current != null
                ? SecurityFeatureSet.BehaviorFeatures.builder()
                        .failedRequestCount(current.getFailedRequestCount())
                        .successfulRequestCount(current.getSuccessfulRequestCount())
                        .failureRatio(current.getFailureRatio())
                        .uniqueUsers(current.getUniqueUsers())
                        .uniqueSourceIps(current.getUniqueSourceIps())
                : SecurityFeatureSet.BehaviorFeatures.builder();

        builder.requestFrequency(reqFreq)
                .uniqueObjectIds(totalUniqueObjectIds > 0 ? totalUniqueObjectIds : uniqueObjectIdsForUser)
                .objectsAccessedPerUser(objectsPerUser)
                .sequentialObjectAccess(sequentialAccess)
                .objectAccessFrequency(objectAccessFrequency)
                .differentResourcesAccessed(stateStore.getUniqueSetSize("resources_" + identity))
                .accessedObjectIds(recentObjectIds)
                .requestedObjectId(primaryObjectId);

        featureSetBuilder.behaviorFeatures(builder.build());
    }

    public List<String> extractObjectIds(ApiSecurityEvent event) {
        if (event == null || event.getRequest() == null) {
            return Collections.emptyList();
        }

        Set<String> resultSet = new LinkedHashSet<>();
        String normalizedEndpoint = event.getRequest().getEndpoint();
        String rawUri = event.getRequest().getRawUri() != null ? event.getRequest().getRawUri() : normalizedEndpoint;

        // 1. Path variables extraction using template comparison
        if (normalizedEndpoint != null && rawUri != null && normalizedEndpoint.contains("{")) {
            String[] normSegs = normalizedEndpoint.split("/");
            String[] rawSegs = rawUri.split("/");
            if (normSegs.length == rawSegs.length) {
                for (int i = 0; i < normSegs.length; i++) {
                    if (normSegs[i].startsWith("{") && normSegs[i].endsWith("}")) {
                        String val = rawSegs[i].trim();
                        if (!val.isEmpty()) {
                            resultSet.add(val);
                        }
                    }
                }
            }
        }

        // 2. Query parameters extraction
        if (event.getRequest().getQueryParameterMap() != null) {
            List<String> configuredNames = properties.getDetection().getObjectAccess().getIdParameterNames();
            Map<String, List<String>> paramMap = event.getRequest().getQueryParameterMap();
            for (String paramName : configuredNames) {
                for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(paramName)) {
                        for (String val : entry.getValue()) {
                            if (val != null && !val.isBlank() && !"[REDACTED]".equals(val)) {
                                resultSet.add(val.trim());
                            }
                        }
                    }
                }
            }
        }

        // 3. Fallback numeric / UUID path segment extraction
        if (resultSet.isEmpty() && rawUri != null) {
            String[] segments = rawUri.split("/");
            for (String seg : segments) {
                String trimmed = seg.trim();
                if (isLikelyObjectId(trimmed)) {
                    resultSet.add(trimmed);
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    private boolean isLikelyObjectId(String seg) {
        if (seg.isEmpty()) return false;
        if (NUMERIC_PATTERN.matcher(seg).matches()) return true;
        if (UUID_PATTERN.matcher(seg).matches()) return true;
        return false;
    }
}
