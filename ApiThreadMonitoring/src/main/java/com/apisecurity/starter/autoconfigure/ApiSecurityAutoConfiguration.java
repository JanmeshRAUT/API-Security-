package com.apisecurity.starter.autoconfigure;

import com.apisecurity.starter.client.DefaultSecurityPlatformClient;
import com.apisecurity.starter.client.SecurityPlatformClient;
import com.apisecurity.starter.collector.RequestMetadataCollector;
import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.filter.ApiSecurityFilter;
import com.apisecurity.starter.feature.FeaturePublisher;
import com.apisecurity.starter.feature.DefaultFeaturePublisher;
import com.apisecurity.starter.feature.extractor.BolaFeatureExtractor;
import com.apisecurity.starter.feature.extractor.CompositeFeatureExtractor;
import com.apisecurity.starter.feature.extractor.CredentialStuffingFeatureExtractor;
import com.apisecurity.starter.feature.state.BehaviorStateStore;
import com.apisecurity.starter.feature.state.InMemoryBehaviorStateStore;
import com.apisecurity.starter.publisher.ApiSecurityEventPublisher;
import com.apisecurity.starter.publisher.DefaultApiSecurityEventPublisher;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@AutoConfiguration
@EnableConfigurationProperties(ApiSecurityProperties.class)
@ConditionalOnProperty(prefix = "api.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAsync
public class ApiSecurityAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ApiSecurityAutoConfiguration.class);

    private final ApiSecurityProperties properties;

    public ApiSecurityAutoConfiguration(ApiSecurityProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info(" API Security Framework");
        log.info(" Status: ENABLED");
        log.info(" Application ID: {}", properties.getApplicationId());
        log.info(" Monitoring: ACTIVE");
        log.info(" Mode: {}", properties.getBehavior().getMode());
        log.info(" Exclusions: {}", properties.getMonitoring().getExcludedPaths());
        log.info(" Feature Extraction: ACTIVE");
        log.info("========================================");
    }

    @Bean(name = "apiSecurityTaskExecutor")
    public Executor apiSecurityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ApiSecurity-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public RequestMetadataCollector requestMetadataCollector() {
        return new RequestMetadataCollector(properties);
    }

    @Bean
    public SecurityPlatformClient securityPlatformClient() {
        return new DefaultSecurityPlatformClient(properties);
    }

    @Bean
    public BehaviorStateStore behaviorStateStore() {
        return new InMemoryBehaviorStateStore(properties);
    }

    @Bean
    public CompositeFeatureExtractor compositeFeatureExtractor(BehaviorStateStore stateStore) {
        return new CompositeFeatureExtractor(
                List.of(
                        new CredentialStuffingFeatureExtractor(stateStore, properties),
                        new BolaFeatureExtractor(stateStore, properties)
                ),
                properties
        );
    }

    @Bean
    public FeaturePublisher featurePublisher() {
        return new DefaultFeaturePublisher(properties);
    }

    @Bean
    public ApiSecurityEventPublisher apiSecurityEventPublisher(CompositeFeatureExtractor compositeFeatureExtractor, FeaturePublisher featurePublisher) {
        return new DefaultApiSecurityEventPublisher(compositeFeatureExtractor, featurePublisher);
    }

    @Bean
    public ApiSecurityFilter apiSecurityFilter(RequestMetadataCollector collector, 
                                               ApiSecurityEventPublisher publisher) {
        return new ApiSecurityFilter(collector, publisher, properties);
    }
}
