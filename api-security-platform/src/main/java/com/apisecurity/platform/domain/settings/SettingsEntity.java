package com.apisecurity.platform.domain.settings;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsEntity {

    @Id
    private String id; // we'll just use a single row id like "GLOBAL"

    // General
    private Integer dataRetentionDays;
    private Integer incidentRetentionDays;

    // AI Engine
    @Enumerated(EnumType.STRING)
    private AnomalySensitivity anomalySensitivity;
    
    private Boolean autoBanEnabled;
    private Boolean rateLimitSuspicious;

    // Integrations
    private String alertsEmail;
    private String slackWebhookUrl;
    private Boolean slackAlertsEnabled;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
