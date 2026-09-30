package com.apisecurity.starter.filter;

import com.apisecurity.starter.collector.RequestMetadataCollector;
import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.publisher.ApiSecurityEventPublisher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ApiSecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiSecurityFilter.class);

    private final RequestMetadataCollector metadataCollector;
    private final ApiSecurityEventPublisher eventPublisher;
    private final ApiSecurityProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiSecurityFilter(RequestMetadataCollector metadataCollector, 
                             ApiSecurityEventPublisher eventPublisher,
                             ApiSecurityProperties properties) {
        this.metadataCollector = metadataCollector;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        
        List<String> excludedPaths = properties.getMonitoring().getExcludedPaths();
        if (excludedPaths == null || excludedPaths.isEmpty()) {
            return false;
        }
        
        String requestUri = request.getRequestURI();
        return excludedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Proceed with the actual request, ensuring we NEVER block the application's flow
            filterChain.doFilter(request, response);
        } finally {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            
            try {
                // Collect metadata and build the event
                ApiSecurityEvent event = metadataCollector.collectMetadata(request, response, responseTimeMs);
                
                // Publish the event to the asynchronous pipeline
                eventPublisher.publish(event);
            } catch (Exception ex) {
                // Do not break the protected application if monitoring fails
                log.error("API Security Framework Error: Failed to process security event", ex);
            }
        }
    }
}
