package com.apisecurity.starter.event;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ApiSecurityEventBuilder {
    
    public static ApiSecurityEvent.ApiSecurityEventBuilder builder() {
        return ApiSecurityEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .requestId(UUID.randomUUID().toString())
                .timestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }
}
