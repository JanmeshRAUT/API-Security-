package com.apisecurity.platform.domain.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events", indexes = {
    @Index(name = "idx_evt_event_id", columnList = "eventId", unique = true),
    @Index(name = "idx_evt_app_id", columnList = "applicationId"),
    @Index(name = "idx_evt_timestamp", columnList = "timestamp"),
    @Index(name = "idx_evt_endpoint", columnList = "endpoint"),
    @Index(name = "idx_evt_proc_status", columnList = "processingStatus")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String applicationId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String requestId;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false)
    private String endpoint;

    private String clientIp;
    private String sourceIpHash;
    private String userHash;
    private boolean authenticated;
    private int statusCode;
    private long responseTimeMs;
    private Integer requestSize;
    private Integer responseSize;

    @Column(length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus;

    @Column(columnDefinition = "TEXT")
    private String rawFeaturesJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.processingStatus == null) {
            this.processingStatus = ProcessingStatus.PENDING;
        }
    }
}
