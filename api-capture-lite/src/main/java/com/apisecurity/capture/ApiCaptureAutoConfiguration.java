package com.apisecurity.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "api.capture", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(ApiCaptureProperties.class)
public class ApiCaptureAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApiCaptureAutoConfiguration.class);

    private final ApiCaptureProperties properties;
    private final Environment environment;

    public ApiCaptureAutoConfiguration(ApiCaptureProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (properties.getApplicationId() == null || properties.getApplicationId().isBlank()) {
            String appName = environment.getProperty("spring.application.name");
            properties.setApplicationId(appName != null && !appName.isBlank() ? appName : "unknown-app");
        }

        log.info("ApiCapture Lite starting up for application: {} targeting platform: {}", 
                 properties.getApplicationId(), 
                 properties.getPlatform().getBaseUrl());
                 
        if (properties.getPlatform().getApiKey() == null || properties.getPlatform().getApiKey().isBlank()) {
            log.warn("ApiCapture Lite: Platform API Key is empty. Events might be rejected by the platform.");
        }
    }

    @Bean(destroyMethod = "close")
    public EventPublisher eventPublisher(ObjectMapper objectMapper) {
        return new EventPublisher(properties, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<ApiCaptureFilter> apiCaptureFilterRegistration(
            EventPublisher eventPublisher,
            ObjectProvider<UserIdResolver> userIdResolverProvider) {

        ApiCaptureFilter filter = new ApiCaptureFilter(eventPublisher, properties, userIdResolverProvider.getIfAvailable());

        FilterRegistrationBean<ApiCaptureFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(properties.getFilterOrder());
        return registration;
    }
}
