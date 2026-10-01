package com.apisecurity.capture;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

public class ApiCaptureFilter extends OncePerRequestFilter {

    private final EventPublisher eventPublisher;
    private final ApiCaptureProperties properties;
    private final UserIdResolver userIdResolver;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiCaptureFilter(EventPublisher eventPublisher, ApiCaptureProperties properties, UserIdResolver userIdResolver) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.userIdResolver = userIdResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (properties.getExcludedPaths() != null) {
            for (String pattern : properties.getExcludedPaths()) {
                if (pathMatcher.match(pattern, uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                long timeTaken = System.currentTimeMillis() - startTime;
                
                ApiEvent event = new ApiEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setApplicationId(resolveApplicationId());
                event.setTimestamp(Instant.now().toString());
                
                String clientIp = extractClientIp(request);
                event.setClientIp(clientIp);

                ApiEvent.RequestData reqData = new ApiEvent.RequestData();
                reqData.setMethod(request.getMethod());
                String rawUri = request.getRequestURI();
                reqData.setRawUri(rawUri);
                
                Object patternAttr = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                reqData.setEndpoint(patternAttr != null ? patternAttr.toString() : rawUri);
                
                reqData.setSourceIp(clientIp);
                
                if (properties.isCaptureUserAgent()) {
                    String ua = request.getHeader("User-Agent");
                    if (ua != null) {
                        reqData.setUserAgent(ua.length() > 300 ? ua.substring(0, 300) : ua);
                    }
                }
                
                String reqLength = request.getHeader("Content-Length");
                if (reqLength != null) {
                    try { reqData.setRequestSize(Long.parseLong(reqLength)); } catch (NumberFormatException ignored) {}
                }
                
                reqData.setPathDepth(calculatePathDepth(rawUri));
                event.setRequest(reqData);

                ApiEvent.ResponseData resData = new ApiEvent.ResponseData();
                resData.setStatusCode(response.getStatus());
                resData.setResponseTimeMs(timeTaken);
                
                String resLength = response.getHeader("Content-Length");
                if (resLength != null) {
                    try { resData.setResponseSize(Long.parseLong(resLength)); } catch (NumberFormatException ignored) {}
                }
                event.setResponse(resData);

                ApiEvent.IdentityData idData = new ApiEvent.IdentityData();
                String userId = null;
                if (userIdResolver != null) {
                    userId = userIdResolver.resolve(request);
                } else {
                    Principal principal = request.getUserPrincipal();
                    if (principal != null) {
                        userId = principal.getName();
                    }
                }
                
                if (userId != null && !userId.isBlank()) {
                    idData.setAuthenticated(true);
                    idData.setUserId(userId);
                } else {
                    idData.setAuthenticated(false);
                }
                event.setIdentity(idData);

                eventPublisher.publish(event);
            } catch (Throwable t) {
                // Must not affect the response
            }
        }
    }
    
    private String resolveApplicationId() {
        if (properties.getApplicationId() != null && !properties.getApplicationId().isBlank()) {
            return properties.getApplicationId();
        }
        return "unknown-app";
    }

    private int calculatePathDepth(String uri) {
        if (uri == null || uri.isEmpty() || uri.equals("/")) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < uri.length(); i++) {
            if (uri.charAt(i) == '/') {
                count++;
            }
        }
        return uri.endsWith("/") ? count - 1 : count;
    }

    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        List<String> trusted = properties.getTrustedProxies();
        
        if (trusted == null || trusted.isEmpty()) {
            return remoteAddr;
        }

        boolean isTrusted = trusted.contains("*") || trusted.contains(remoteAddr);
        if (!isTrusted) {
            return remoteAddr;
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (xff == null || xff.isBlank()) {
            return remoteAddr;
        }

        String[] parts = xff.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String ip = parts[i].trim();
            if (!trusted.contains("*") && !trusted.contains(ip)) {
                return ip;
            }
        }
        return parts[0].trim();
    }
}
