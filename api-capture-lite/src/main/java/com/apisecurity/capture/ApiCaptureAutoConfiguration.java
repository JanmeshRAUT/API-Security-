package com.apisecurity.capture;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ApiCaptureAutoConfiguration {

    @Bean
    public EventPublisher eventPublisher() {
        return new EventPublisher();
    }

    @Bean
    public ApiCaptureFilter apiCaptureFilter(EventPublisher eventPublisher) {
        return new ApiCaptureFilter(eventPublisher);
    }
}
