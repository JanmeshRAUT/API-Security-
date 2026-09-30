package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;
import com.apisecurity.starter.feature.state.BehaviorStateStore;
import com.apisecurity.starter.feature.state.InMemoryBehaviorStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CredentialStuffingFeatureExtractorTest {

    private BehaviorStateStore stateStore;
    private ApiSecurityProperties properties;
    private CredentialStuffingFeatureExtractor extractor;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryBehaviorStateStore();
        properties = new ApiSecurityProperties();
        extractor = new CredentialStuffingFeatureExtractor(stateStore, properties);
    }

    @Test
    void testExtractLoginFeatures() {
        // 1st request (failed)
        ApiSecurityEvent event1 = createEvent("/api/auth/login", "10.0.0.1", null, 401);
        SecurityFeatureSet.SecurityFeatureSetBuilder builder1 = SecurityFeatureSet.builder();
        extractor.extract(event1, builder1);
        SecurityFeatureSet set1 = builder1.build();
        assertEquals(1, set1.getBehaviorFeatures().getRequestFrequency());
        assertEquals(1, set1.getBehaviorFeatures().getFailedRequestCount());
        assertEquals(0, set1.getBehaviorFeatures().getUniqueUsers());

        // 2nd request (success with user)
        ApiSecurityEvent event2 = createEvent("/api/auth/login", "10.0.0.1", "user1", 200);
        SecurityFeatureSet.SecurityFeatureSetBuilder builder2 = SecurityFeatureSet.builder();
        extractor.extract(event2, builder2);
        SecurityFeatureSet set2 = builder2.build();
        assertEquals(2, set2.getBehaviorFeatures().getRequestFrequency());
        assertEquals(1, set2.getBehaviorFeatures().getFailedRequestCount()); // Still 1 failed
        assertEquals(1, set2.getBehaviorFeatures().getUniqueUsers());
    }

    @Test
    void testIgnoresNonLoginEndpoints() {
        SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
        ApiSecurityEvent event = createEvent("/api/products", "10.0.0.1", null, 200);
        
        extractor.extract(event, builder);
        SecurityFeatureSet set = builder.build();
        
        assertNull(set.getBehaviorFeatures());
    }

    private ApiSecurityEvent createEvent(String endpoint, String sourceIp, String userId, int statusCode) {
        return ApiSecurityEvent.builder()
                .request(ApiSecurityEvent.RequestMetadata.builder().endpoint(endpoint).sourceIp(sourceIp).build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder().userId(userId).build())
                .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(statusCode).build())
                .build();
    }
}
