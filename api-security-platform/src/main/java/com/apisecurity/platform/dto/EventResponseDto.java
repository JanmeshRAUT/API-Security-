package com.apisecurity.platform.dto;

import com.apisecurity.platform.domain.event.ProcessingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

    private String id;
    private String eventId;
    private String applicationId;
    private LocalDateTime timestamp;
    private String requestId;
    private String httpMethod;
    private String endpoint;
    private String clientIp;
    private String sourceIpHash;
    private String userHash;
    private boolean authenticated;
    private int statusCode;
    private long responseTimeMs;
    private Integer requestSize;
    private Integer responseSize;
    private String userAgent;
    private ProcessingStatus processingStatus;
    private LocalDateTime createdAt;
}
