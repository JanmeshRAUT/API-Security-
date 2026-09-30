package com.apisecurity.starter.feature.extractor;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.feature.SecurityFeatureSet;
import com.apisecurity.starter.feature.state.InMemoryBehaviorStateStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnhancedFeatureExtractionTest {

    private InMemoryBehaviorStateStore stateStore;
    private ApiSecurityProperties properties;
    private BolaFeatureExtractor bolaExtractor;
    private CredentialStuffingFeatureExtractor credentialExtractor;
    private CompositeFeatureExtractor compositeExtractor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        properties = new ApiSecurityProperties();
        stateStore = new InMemoryBehaviorStateStore(properties);
        bolaExtractor = new BolaFeatureExtractor(stateStore, properties);
        credentialExtractor = new CredentialStuffingFeatureExtractor(stateStore, properties);
        compositeExtractor = new CompositeFeatureExtractor(List.of(credentialExtractor, bolaExtractor), properties);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testObjectIdExtractionFromPathVariable() {
        ApiSecurityEvent event = ApiSecurityEvent.builder()
                .request(ApiSecurityEvent.RequestMetadata.builder()
                        .endpoint("/api/orders/{id}")
                        .rawUri("/api/orders/101")
                        .sourceIp("192.168.1.1")
                        .build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("user1").build())
                .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                .build();

        List<String> ids = bolaExtractor.extractObjectIds(event);
        assertEquals(1, ids.size());
        assertEquals("101", ids.get(0));

        SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
        compositeExtractor.extract(event, builder);
        SecurityFeatureSet set = builder.build();

        assertEquals("101", set.getBehaviorFeatures().getRequestedObjectId());
    }

    @Test
    void testObjectIdExtractionFromMultiplePathVariables() {
        ApiSecurityEvent event = ApiSecurityEvent.builder()
                .request(ApiSecurityEvent.RequestMetadata.builder()
                        .endpoint("/api/orders/{orderId}/items/{itemId}")
                        .rawUri("/api/orders/101/items/55")
                        .sourceIp("192.168.1.1")
                        .build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("user1").build())
                .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                .build();

        List<String> ids = bolaExtractor.extractObjectIds(event);
        assertEquals(2, ids.size());
        assertTrue(ids.contains("101"));
        assertTrue(ids.contains("55"));
    }

    @Test
    void testObjectIdExtractionFromQueryParams() {
        ApiSecurityEvent event = ApiSecurityEvent.builder()
                .request(ApiSecurityEvent.RequestMetadata.builder()
                        .endpoint("/api/orders")
                        .rawUri("/api/orders?id=101")
                        .queryParameterMap(Map.of("id", List.of("101")))
                        .sourceIp("192.168.1.1")
                        .build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("user1").build())
                .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                .build();

        List<String> ids = bolaExtractor.extractObjectIds(event);
        assertEquals(1, ids.size());
        assertEquals("101", ids.get(0));
    }

    @Test
    void testSequentialAccessDetection() {
        String[] sequentialIds = {"101", "102", "103", "104"};
        boolean finalSeq = false;

        for (String id : sequentialIds) {
            ApiSecurityEvent event = ApiSecurityEvent.builder()
                    .request(ApiSecurityEvent.RequestMetadata.builder()
                            .endpoint("/api/orders/{id}")
                            .rawUri("/api/orders/" + id)
                            .sourceIp("10.0.0.1")
                            .build())
                    .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("attacker").build())
                    .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                    .build();

            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            compositeExtractor.extract(event, builder);
            finalSeq = builder.build().getBehaviorFeatures().isSequentialObjectAccess();
        }

        assertTrue(finalSeq, "Sequential access pattern 101->102->103->104 should trigger sequentialObjectAccess=true");
    }

    @Test
    void testNonSequentialAccessDetection() {
        String[] randomIds = {"101", "305", "72", "911"};
        boolean finalSeq = false;

        for (String id : randomIds) {
            ApiSecurityEvent event = ApiSecurityEvent.builder()
                    .request(ApiSecurityEvent.RequestMetadata.builder()
                            .endpoint("/api/orders/{id}")
                            .rawUri("/api/orders/" + id)
                            .sourceIp("10.0.0.1")
                            .build())
                    .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("normalUser").build())
                    .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                    .build();

            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            compositeExtractor.extract(event, builder);
            finalSeq = builder.build().getBehaviorFeatures().isSequentialObjectAccess();
        }

        assertFalse(finalSeq, "Random access pattern 101->305->72->911 should NOT trigger sequentialObjectAccess");
    }

    @Test
    void testUniqueObjectCount() {
        String[] ids = {"101", "101", "102", "103"};
        SecurityFeatureSet lastSet = null;

        for (String id : ids) {
            ApiSecurityEvent event = ApiSecurityEvent.builder()
                    .request(ApiSecurityEvent.RequestMetadata.builder()
                            .endpoint("/api/orders/{id}")
                            .rawUri("/api/orders/" + id)
                            .sourceIp("10.0.0.1")
                            .build())
                    .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("user1").build())
                    .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                    .build();

            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            compositeExtractor.extract(event, builder);
            lastSet = builder.build();
        }

        assertNotNull(lastSet);
        assertEquals(3, lastSet.getBehaviorFeatures().getUniqueObjectIds());
    }

    @Test
    void testFailureRatioCalculation() {
        String loginEndpoint = "/api/auth/login";
        String ip = "10.0.0.99";

        // 8 successful requests
        for (int i = 0; i < 8; i++) {
            ApiSecurityEvent event = ApiSecurityEvent.builder()
                    .request(ApiSecurityEvent.RequestMetadata.builder().endpoint(loginEndpoint).sourceIp(ip).build())
                    .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("user_" + i).build())
                    .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(200).build())
                    .build();
            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            compositeExtractor.extract(event, builder);
        }

        // 2 failed requests
        SecurityFeatureSet lastSet = null;
        for (int i = 0; i < 2; i++) {
            ApiSecurityEvent event = ApiSecurityEvent.builder()
                    .request(ApiSecurityEvent.RequestMetadata.builder().endpoint(loginEndpoint).sourceIp(ip).build())
                    .identity(ApiSecurityEvent.IdentityMetadata.builder().userId("bad_user_" + i).build())
                    .response(ApiSecurityEvent.ResponseMetadata.builder().statusCode(401).build())
                    .build();
            SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
            compositeExtractor.extract(event, builder);
            lastSet = builder.build();
        }

        assertNotNull(lastSet);
        assertEquals(10, lastSet.getBehaviorFeatures().getRequestFrequency());
        assertEquals(2, lastSet.getBehaviorFeatures().getFailedRequestCount());
        assertEquals(8, lastSet.getBehaviorFeatures().getSuccessfulRequestCount());
        assertEquals(0.2, lastSet.getBehaviorFeatures().getFailureRatio(), 0.001);
    }

    @Test
    void testJsonSerializationCompatibility() throws Exception {
        ApiSecurityEvent event = ApiSecurityEvent.builder()
                .eventId("evt-12345")
                .applicationId("ShopSphere")
                .timestamp("2026-09-07T12:00:00Z")
                .request(ApiSecurityEvent.RequestMetadata.builder()
                        .method("GET")
                        .endpoint("/api/orders/{id}")
                        .rawUri("/api/orders/101")
                        .sourceIp("192.168.1.50")
                        .userAgent("Mozilla/5.0")
                        .build())
                .identity(ApiSecurityEvent.IdentityMetadata.builder()
                        .authenticated(true)
                        .userId("user_42")
                        .build())
                .response(ApiSecurityEvent.ResponseMetadata.builder()
                        .statusCode(200)
                        .responseTimeMs(84)
                        .build())
                .build();

        SecurityFeatureSet.SecurityFeatureSetBuilder builder = SecurityFeatureSet.builder();
        compositeExtractor.extract(event, builder);
        SecurityFeatureSet featureSet = builder.build();

        String json = objectMapper.writeValueAsString(featureSet);
        assertNotNull(json);

        // Verify key structural fields present in JSON
        assertTrue(json.contains("\"eventId\":\"evt-12345\""));
        assertTrue(json.contains("\"applicationId\":\"ShopSphere\""));
        assertTrue(json.contains("\"requestFeatures\""));
        assertTrue(json.contains("\"identityFeatures\""));
        assertTrue(json.contains("\"networkFeatures\""));
        assertTrue(json.contains("\"behaviorFeatures\""));
        assertTrue(json.contains("\"sequentialObjectAccess\""));
        assertTrue(json.contains("\"uniqueObjectIds\""));
    }
}
