package com.apisecurity.platform.domain.threat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "threats", indexes = {
    @Index(name = "idx_tht_app_id", columnList = "applicationId"),
    @Index(name = "idx_tht_type", columnList = "threatType"),
    @Index(name = "idx_tht_severity", columnList = "severity"),
    @Index(name = "idx_tht_status", columnList = "status"),
    @Index(name = "idx_tht_detected_at", columnList = "detectedAt"),
    @Index(name = "idx_tht_endpoint", columnList = "endpoint"),
    @Index(name = "idx_tht_event_id", columnList = "eventId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatType threatType;

    private double confidence;
    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecommendedAction recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThreatStatus status;

    @Column(columnDefinition = "TEXT")
    private String reasonCodes; // JSON list string

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    private String endpoint;
    private String sourceIpHash;
    private String userHash;
    private String httpMethod;
    private int statusCode;

    @PrePersist
    protected void onCreate() {
        if (this.detectedAt == null) {
            this.detectedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ThreatStatus.OPEN;
        }
    }
}
