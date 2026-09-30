package com.apisecurity.starter.client;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

public class DefaultSecurityPlatformClient implements SecurityPlatformClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultSecurityPlatformClient.class);
    
    private final ApiSecurityProperties properties;
    private final RestTemplate restTemplate;

    public DefaultSecurityPlatformClient(ApiSecurityProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Async("apiSecurityTaskExecutor")
    public void sendEvent(ApiSecurityEvent event) {
        try {
            String url = properties.getPlatform().getBaseUrl() + "/api/v1/events";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", properties.getPlatform().getApiKey());

            HttpEntity<ApiSecurityEvent> entity = new HttpEntity<>(event, headers);
            restTemplate.postForEntity(url, entity, String.class);
            log.debug("Sent security event to platform [{}]: {}", url, event.getRequestId());
        } catch (Exception e) {
            log.error("Failed to send security event to platform: {}", e.getMessage());
        }
    }
}
