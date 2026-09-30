package com.apisecurity.starter.filter;

import com.apisecurity.starter.collector.RequestMetadataCollector;
import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.publisher.ApiSecurityEventPublisher;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ApiSecurityFilterTest {

    private RequestMetadataCollector metadataCollector;
    private ApiSecurityEventPublisher eventPublisher;
    private ApiSecurityProperties properties;
    private ApiSecurityFilter filter;

    @BeforeEach
    void setUp() {
        metadataCollector = mock(RequestMetadataCollector.class);
        eventPublisher = mock(ApiSecurityEventPublisher.class);
        properties = new ApiSecurityProperties();
        
        filter = new ApiSecurityFilter(metadataCollector, eventPublisher, properties);
    }

    @Test
    void testFilterInterceptsAndPublishesEvent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        ApiSecurityEvent dummyEvent = ApiSecurityEvent.builder().build();
        when(metadataCollector.collectMetadata(any(), any(), anyLong())).thenReturn(dummyEvent);

        filter.doFilter(request, response, filterChain);

        verify(metadataCollector, times(1)).collectMetadata(eq(request), eq(response), anyLong());
        verify(eventPublisher, times(1)).publish(dummyEvent);
    }

    @Test
    void testFilterDoesNotBlockOnCollectorFailure() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Mock chain to prove it executes even if collector throws exception
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(metadataCollector.collectMetadata(any(), any(), anyLong())).thenThrow(new RuntimeException("Simulated error"));

        assertDoesNotThrow(() -> filter.doFilter(request, response, filterChain));

        verify(filterChain, times(1)).doFilter(request, response);
        verify(eventPublisher, never()).publish(any());
    }
    
    @Test
    void testExcludedPathsAreNotMonitored() throws ServletException, IOException {
        properties.getMonitoring().setExcludedPaths(java.util.List.of("/actuator/**"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(metadataCollector, never()).collectMetadata(any(), any(), anyLong());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void testFrameworkDisabled() throws ServletException, IOException {
        properties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(metadataCollector, never()).collectMetadata(any(), any(), anyLong());
    }
}
