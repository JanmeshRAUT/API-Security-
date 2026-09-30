package com.apisecurity.starter.publisher;

import com.apisecurity.starter.event.ApiSecurityEvent;

public interface ApiSecurityEventPublisher {
    
    /**
     * Publishes a security event to the pipeline.
     * Implementations must not block the calling thread for extended periods.
     *
     * @param event The security event to publish.
     */
    void publish(ApiSecurityEvent event);
}
