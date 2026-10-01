package com.apisecurity.capture;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class ApiCaptureFilter extends OncePerRequestFilter {

    private final EventPublisher eventPublisher;

    public ApiCaptureFilter(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Proceed with the actual request
            filterChain.doFilter(request, response);
        } finally {
            // Capture response details after the request is processed
            long timeTaken = System.currentTimeMillis() - startTime;
            
            ApiEvent event = new ApiEvent();
            event.setTimestamp(Instant.now().toString());
            event.setMethod(request.getMethod());
            event.setEndpoint(request.getRequestURI());
            event.setIpAddress(request.getRemoteAddr());
            event.setStatusCode(response.getStatus());
            event.setResponseTime(timeTaken);
            
            // Send the event asynchronously to the platform
            eventPublisher.publish(event);
        }
    }
}
