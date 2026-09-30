package com.apisecurity.starter.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ApiSecurityEvent {
    private String eventId;
    private String requestId;
    private String applicationId;
    private String timestamp;

    private RequestMetadata request;
    private IdentityMetadata identity;
    private ResponseMetadata response;

    @Data
    @Builder
    public static class RequestMetadata {
        private String method;
        private String endpoint;
        private String rawUri;
        private String sourceIp;
        private String userAgent;
        private List<String> queryParameters;
        private Map<String, List<String>> queryParameterMap;
        private Integer requestSize;
        private Integer pathDepth;
    }

    @Data
    @Builder
    public static class IdentityMetadata {
        private boolean authenticated;
        private String userId;
        private String targetAccountId;
        private String sessionId;
    }

    @Data
    @Builder
    public static class ResponseMetadata {
        private int statusCode;
        private long responseTimeMs;
        private Integer responseSize;
    }
}
