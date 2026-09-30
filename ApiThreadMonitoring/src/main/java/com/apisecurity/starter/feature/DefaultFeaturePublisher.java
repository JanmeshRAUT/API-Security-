package com.apisecurity.starter.feature;

import com.apisecurity.starter.config.ApiSecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

public class DefaultFeaturePublisher implements FeaturePublisher {

    private static final Logger log = LoggerFactory.getLogger(DefaultFeaturePublisher.class);

    private final ApiSecurityProperties properties;
    private final RestTemplate restTemplate;

    public DefaultFeaturePublisher(ApiSecurityProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Override
    @Async("apiSecurityTaskExecutor")
    public void publish(SecurityFeatureSet featureSet) {
        try {
            String url = properties.getPlatform().getBaseUrl() + "/api/v1/events";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", properties.getPlatform().getApiKey());

            HttpEntity<SecurityFeatureSet> entity = new HttpEntity<>(featureSet, headers);
            restTemplate.postForEntity(url, entity, String.class);
            log.debug("Published SecurityFeatureSet event [{}] to platform: {}", featureSet.getEventId(), url);
        } catch (Exception e) {
            log.error("Failed to publish SecurityFeatureSet to platform: {}", e.getMessage());
        }
    }
}
