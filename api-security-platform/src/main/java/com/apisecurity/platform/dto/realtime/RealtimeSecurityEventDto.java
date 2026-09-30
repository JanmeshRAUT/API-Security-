package com.apisecurity.platform.dto.realtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeSecurityEventDto {

    private String id;
    private String eventId;
    private String applicationId;
    private LocalDateTime timestamp;
    private String httpMethod;
    private String endpoint;
    private String clientIp;
    private int statusCode;
    private long responseTimeMs;
    private Integer requestSize;
    private Integer responseSize;
    private boolean authenticated;
    private String sourceIpHash;
    private String userHash;
}
