package com.apisecurity.capture;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "api.capture")
public class ApiCaptureProperties {

    private boolean enabled = true;
    private String applicationId;
    
    private Platform platform = new Platform();
    private Publisher publisher = new Publisher();

    private List<String> excludedPaths = Arrays.asList(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error"
    );
    private List<String> trustedProxies = Arrays.asList();
    private boolean captureUserAgent = true;
    private int filterOrder = Ordered.HIGHEST_PRECEDENCE + 10;

    public static class Platform {
        private String baseUrl = "http://localhost:8085";
        private String path = "/api/v1/events";
        private String apiKey = "";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }

    public static class Publisher {
        private int workerThreads = 2;
        private int queueCapacity = 1000;
        private long connectTimeoutMs = 2000;
        private long requestTimeoutMs = 3000;

        public int getWorkerThreads() { return workerThreads; }
        public void setWorkerThreads(int workerThreads) { this.workerThreads = workerThreads; }
        public int getQueueCapacity() { return queueCapacity; }
        public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
        public long getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(long connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public long getRequestTimeoutMs() { return requestTimeoutMs; }
        public void setRequestTimeoutMs(long requestTimeoutMs) { this.requestTimeoutMs = requestTimeoutMs; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public Platform getPlatform() { return platform; }
    public void setPlatform(Platform platform) { this.platform = platform; }
    public Publisher getPublisher() { return publisher; }
    public void setPublisher(Publisher publisher) { this.publisher = publisher; }
    public List<String> getExcludedPaths() { return excludedPaths; }
    public void setExcludedPaths(List<String> excludedPaths) { this.excludedPaths = excludedPaths; }
    public List<String> getTrustedProxies() { return trustedProxies; }
    public void setTrustedProxies(List<String> trustedProxies) { this.trustedProxies = trustedProxies; }
    public boolean isCaptureUserAgent() { return captureUserAgent; }
    public void setCaptureUserAgent(boolean captureUserAgent) { this.captureUserAgent = captureUserAgent; }
    public int getFilterOrder() { return filterOrder; }
    public void setFilterOrder(int filterOrder) { this.filterOrder = filterOrder; }
}
