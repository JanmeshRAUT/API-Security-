package com.apisecurity.starter.collector;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestMetadataCollectorTest {

    private ApiSecurityProperties properties;
    private RequestMetadataCollector collector;

    @BeforeEach
    void setUp() {
        properties = new ApiSecurityProperties();
        collector = new RequestMetadataCollector(properties);
    }

    @Test
    void testCollectsBasicMetadata() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/123");
        request.addHeader("User-Agent", "Test-Agent");
        request.setParameter("active", "true");
        request.setRemoteAddr("10.0.0.1");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        ApiSecurityEvent event = collector.collectMetadata(request, response, 150L);

        assertNotNull(event.getEventId());
        assertEquals("default-application", event.getApplicationId());
        
        // Request Metadata
        assertEquals("GET", event.getRequest().getMethod());
        assertEquals("/api/users/123", event.getRequest().getEndpoint());
        assertEquals("Test-Agent", event.getRequest().getUserAgent());
        assertEquals("10.0.0.1", event.getRequest().getSourceIp());
        assertTrue(event.getRequest().getQueryParameters().contains("active"));

        // Identity Metadata
        assertFalse(event.getIdentity().isAuthenticated());
        assertNull(event.getIdentity().getUserId());

        // Response Metadata
        assertEquals(200, event.getResponse().getStatusCode());
        assertEquals(150L, event.getResponse().getResponseTimeMs());
    }

    @Test
    void testAuthenticatedUser() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setUserPrincipal(() -> "user-456");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiSecurityEvent event = collector.collectMetadata(request, response, 50L);

        assertTrue(event.getIdentity().isAuthenticated());
        assertEquals("user-456", event.getIdentity().getUserId());
    }

    @Test
    void testEndpointNormalization() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/123");
        // Simulate Spring's path matching logic mapping to a variable
        request.setAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern", "/api/orders/{id}");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiSecurityEvent event = collector.collectMetadata(request, response, 50L);

        assertEquals("/api/orders/{id}", event.getRequest().getEndpoint());
    }

    @Test
    void testTrustedProxyExtraction() {
        properties.getMonitoring().setTrustedProxies(List.of("192.168.1.100"));
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("X-Forwarded-For", "203.0.113.1, 192.168.1.50");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiSecurityEvent event = collector.collectMetadata(request, response, 50L);

        assertEquals("203.0.113.1", event.getRequest().getSourceIp());
    }
    
    @Test
    void testUntrustedProxyExtractionIgnored() {
        properties.getMonitoring().setTrustedProxies(List.of("192.168.1.100"));
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("10.0.0.5"); // Not a trusted proxy
        request.addHeader("X-Forwarded-For", "203.0.113.1");
        
        MockHttpServletResponse response = new MockHttpServletResponse();

        ApiSecurityEvent event = collector.collectMetadata(request, response, 50L);

        assertEquals("10.0.0.5", event.getRequest().getSourceIp()); // Should fall back to remote addr
    }
}
