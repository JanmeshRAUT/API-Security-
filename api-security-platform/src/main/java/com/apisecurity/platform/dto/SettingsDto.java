package com.apisecurity.platform.dto;

import com.apisecurity.platform.domain.settings.AnomalySensitivity;
import lombok.Data;

@Data
public class SettingsDto {
    private Integer dataRetentionDays;
    private Integer incidentRetentionDays;
    private AnomalySensitivity anomalySensitivity;
    private Boolean autoBanEnabled;
    private Boolean rateLimitSuspicious;
    private String alertsEmail;
    private String slackWebhookUrl;
    private Boolean slackAlertsEnabled;
}
