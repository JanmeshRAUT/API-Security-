package com.apisecurity.starter.autoconfigure;

import com.apisecurity.starter.collector.RequestMetadataCollector;
import com.apisecurity.starter.filter.ApiSecurityFilter;
import com.apisecurity.starter.publisher.ApiSecurityEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ApiSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ApiSecurityAutoConfiguration.class));

    @Test
    void testAutoConfigurationLoadsBeansWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ApiSecurityFilter.class);
            assertThat(context).hasSingleBean(RequestMetadataCollector.class);
            assertThat(context).hasSingleBean(ApiSecurityEventPublisher.class);
        });
    }

    @Test
    void testAutoConfigurationDoesNotLoadWhenDisabled() {
        contextRunner.withPropertyValues("api.security.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ApiSecurityFilter.class);
                    assertThat(context).doesNotHaveBean(RequestMetadataCollector.class);
                    assertThat(context).doesNotHaveBean(ApiSecurityEventPublisher.class);
                });
    }
}
