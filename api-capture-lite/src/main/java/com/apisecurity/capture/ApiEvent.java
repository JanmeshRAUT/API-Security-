package com.apisecurity.capture;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiEvent {

    private String eventId;
    private String applicationId;
    private String timestamp;
    private String clientIp;

    private RequestData request;
    private ResponseData response;
    private IdentityData identity;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public RequestData getRequest() { return request; }
    public void setRequest(RequestData request) { this.request = request; }
    public ResponseData getResponse() { return response; }
    public void setResponse(ResponseData response) { this.response = response; }
    public IdentityData getIdentity() { return identity; }
    public void setIdentity(IdentityData identity) { this.identity = identity; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestData {
        private String method;
        private String endpoint;
        private String rawUri;
        private String sourceIp;
        private String userAgent;
        private Long requestSize;
        private Integer pathDepth;

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getRawUri() { return rawUri; }
        public void setRawUri(String rawUri) { this.rawUri = rawUri; }
        public String getSourceIp() { return sourceIp; }
        public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public Long getRequestSize() { return requestSize; }
        public void setRequestSize(Long requestSize) { this.requestSize = requestSize; }
        public Integer getPathDepth() { return pathDepth; }
        public void setPathDepth(Integer pathDepth) { this.pathDepth = pathDepth; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseData {
        private Integer statusCode;
        private Long responseTimeMs;
        private Long responseSize;

        public Integer getStatusCode() { return statusCode; }
        public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
        public Long getResponseTimeMs() { return responseTimeMs; }
        public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
        public Long getResponseSize() { return responseSize; }
        public void setResponseSize(Long responseSize) { this.responseSize = responseSize; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class IdentityData {
        private boolean authenticated;
        private String userId;

        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
