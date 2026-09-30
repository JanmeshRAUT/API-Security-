package com.apisecurity.starter.publisher;

import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.FeaturePublisher;
import com.apisecurity.starter.feature.SecurityFeatureSet;
import com.apisecurity.starter.feature.extractor.CompositeFeatureExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

public class DefaultApiSecurityEventPublisher implements ApiSecurityEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DefaultApiSecurityEventPublisher.class);
    
    private final CompositeFeatureExtractor featureExtractor;
    private final FeaturePublisher featurePublisher;

    public DefaultApiSecurityEventPublisher(CompositeFeatureExtractor featureExtractor, FeaturePublisher featurePublisher) {
        this.featureExtractor = featureExtractor;
        this.featurePublisher = featurePublisher;
    }

    @Override
    @Async("apiSecurityTaskExecutor")
    public void publish(ApiSecurityEvent event) {
        try {
            log.debug("Processing API security event: {}", event.getEventId());
            
            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            featureExtractor.extract(event, builder);
            
            SecurityFeatureSet featureSet = builder.build();
            featurePublisher.publish(featureSet);
            
        } catch (Exception ex) {
            log.error("Failed to process security event [{}]: {}", event.getEventId(), ex.getMessage());
        }
    }
}
