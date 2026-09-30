package com.apisecurity.starter.collector;

import com.apisecurity.starter.config.ApiSecurityProperties;
import com.apisecurity.starter.event.ApiSecurityEvent;
import com.apisecurity.starter.event.ApiSecurityEventBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.security.Principal;
import java.util.*;

public class RequestMetadataCollector {

    private static final Logger log = LoggerFactory.getLogger(RequestMetadataCollector.class);
    private static final Set<String> SENSITIVE_PARAM_KEYWORDS = Set.of(
            "password", "secret", "token", "apikey", "auth", "credential", "private", "card"
    );

    private final ApiSecurityProperties properties;

    public RequestMetadataCollector(ApiSecurityProperties properties) {
        this.properties = properties;
    }

    public ApiSecurityEvent collectMetadata(HttpServletRequest request, HttpServletResponse response, long responseTimeMs) {
        String rawUri = request.getRequestURI();
        Map<String, List<String>> queryParamMap = extractQueryParameterMap(request);

        ApiSecurityEvent.RequestMetadata requestMetadata = ApiSecurityEvent.RequestMetadata.builder()
                .method(request.getMethod())
                .endpoint(getNormalizedEndpoint(request))
                .rawUri(rawUri)
                .sourceIp(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .queryParameters(new ArrayList<>(queryParamMap.keySet()))
                .queryParameterMap(queryParamMap)
                .requestSize(parseContentLength(request.getHeader("Content-Length")))
                .pathDepth(calculatePathDepth(rawUri))
                .build();
                
        Principal principal = request.getUserPrincipal();
        String userId = Optional.ofNullable(principal)
                .map(Principal::getName)
                .orElse(null);
        String targetAccountId = extractTargetAccountId(request, queryParamMap);
                
        ApiSecurityEvent.IdentityMetadata identityMetadata = ApiSecurityEvent.IdentityMetadata.builder()
                .authenticated(userId != null)
                .userId(userId)
                .targetAccountId(targetAccountId)
                .sessionId(request.getRequestedSessionId())
                .build();

        ApiSecurityEvent.ResponseMetadata responseMetadata = ApiSecurityEvent.ResponseMetadata.builder()
                .statusCode(response.getStatus())
                .responseTimeMs(responseTimeMs)
                .responseSize(parseContentLength(response.getHeader("Content-Length")))
                .build();

        ApiSecurityEvent event = ApiSecurityEventBuilder.builder().build();
        event.setApplicationId(properties.getApplicationId());
        event.setRequest(requestMetadata);
        event.setIdentity(identityMetadata);
        event.setResponse(responseMetadata);

        return event;
    }
    
    private String getNormalizedEndpoint(HttpServletRequest request) {
        // Use Spring's BEST_MATCHING_PATTERN_ATTRIBUTE if available
        Object pattern = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        if (pattern != null) {
            return pattern.toString();
        }
        return request.getRequestURI();
    }

    private Map<String, List<String>> extractQueryParameterMap(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null || paramMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> safeMap = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String key = entry.getKey();
            if (isSensitiveKey(key)) {
                safeMap.put(key, List.of("[REDACTED]"));
            } else {
                safeMap.put(key, entry.getValue() != null ? Arrays.asList(entry.getValue()) : Collections.emptyList());
            }
        }
        return safeMap;
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_PARAM_KEYWORDS.stream().anyMatch(lowerKey::contains);
    }

    private Integer parseContentLength(String lengthHeader) {
        if (lengthHeader == null || lengthHeader.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(lengthHeader.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int calculatePathDepth(String uri) {
        if (uri == null || uri.isBlank() || "/".equals(uri)) {
            return 0;
        }
        String cleaned = uri.startsWith("/") ? uri.substring(1) : uri;
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) return 0;
        return cleaned.split("/").length;
    }

    private String extractTargetAccountId(HttpServletRequest request, Map<String, List<String>> queryParamMap) {
        List<String> accountKeys = List.of("username", "userId", "targetAccountId", "account", "orderId");
        for (String key : accountKeys) {
            if (queryParamMap.containsKey(key)) {
                List<String> vals = queryParamMap.get(key);
                if (vals != null && !vals.isEmpty() && !"[REDACTED]".equals(vals.get(0))) {
                    return vals.get(0);
                }
            }
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        List<String> trustedProxies = properties.getMonitoring().getTrustedProxies();
        String remoteAddr = request.getRemoteAddr();
        
        // If we don't have trusted proxies configured, we must not trust X-Forwarded-For
        if (trustedProxies == null || trustedProxies.isEmpty()) {
            return remoteAddr;
        }

        // Simplistic check for trusted proxy (in a real app, use CIDR matching if needed)
        // For starter purposes, if the remote address is in trusted proxies, we can look at XFF.
        if (trustedProxies.contains(remoteAddr) || trustedProxies.contains("*")) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.split(",")[0].trim();
            }
        }
        
        return remoteAddr;
    }
}
