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

class BolaFeatureExtractorTest {

    private BehaviorStateStore stateStore;
    private ApiSecurityProperties properties;
    private BolaFeatureExtractor extractor;

    @BeforeEach
    void setUp() {
        stateStore = new InMemoryBehaviorStateStore();
        properties = new ApiSecurityProperties();
        extractor = new BolaFeatureExtractor(stateStore, properties);
    }

    @Test
    void testExtractsObjectAccess() {
        ApiSecurityEvent event1 = createEvent("/api/orders/{id}", "user1");
        SecurityFeatureSet.SecurityFeatureSetBuilder builder1 = SecurityFeatureSet.builder();
        extractor.extract(event1, builder1);
        
        SecurityFeatureSet set1 = builder1.build();
        assertEquals(1, set1.getBehaviorFeatures().getRequestFrequency());
        assertEquals(1, set1.getBehaviorFeatures().getUniqueObjectIds());

        ApiSecurityEvent event2 = createEvent("/api/orders/{id}", "user1");
        SecurityFeatureSet.SecurityFeatureSetBuilder builder2 = SecurityFeatureSet.builder();
        extractor.extract(event2, builder2);
        
        SecurityFeatureSet set2 = builder2.build();
        assertEquals(2, set2.getBehaviorFeatures().getRequestFrequency());
        assertEquals(1, set2.getBehaviorFeatures().getUniqueObjectIds());
    }

    @Test
    void testIgnoresNonObjectEndpoints() {
        SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
        ApiSecurityEvent event = createEvent("/api/orders", "user1");
        
        extractor.extract(event, builder);
        SecurityFeatureSet set = builder.build();
        
        assertNull(set.getBehaviorFeatures());
    }

    private ApiSecurityEvent createEvent(String endpoint, String userId) {
        return ApiSecurityEvent.builder()
                .request(ApiSecurityEvent.RequestMetadata.builder().endpoint(endpoint).sourceIp("10.0.0.1").build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder().userId(userId).build())
                .build();
    }
}
