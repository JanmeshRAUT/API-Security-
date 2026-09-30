package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.settings.AnomalySensitivity;
import com.apisecurity.platform.domain.settings.SettingsEntity;
import com.apisecurity.platform.dto.SettingsDto;
import com.apisecurity.platform.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private static final String GLOBAL_SETTINGS_ID = "GLOBAL";

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    public SettingsEntity getSettings() {
        return settingsRepository.findById(GLOBAL_SETTINGS_ID)
                .orElseGet(() -> {
                    // Create default settings if not exist
                    SettingsEntity defaultSettings = SettingsEntity.builder()
                            .id(GLOBAL_SETTINGS_ID)
                            .dataRetentionDays(14)
                            .incidentRetentionDays(365)
                            .anomalySensitivity(AnomalySensitivity.MEDIUM)
                            .autoBanEnabled(true)
                            .rateLimitSuspicious(true)
                            .alertsEmail("soc-alerts@company.com")
                            .slackWebhookUrl("https://hooks.slack.com/services/...")
                            .slackAlertsEnabled(true)
                            .build();
                    return settingsRepository.save(defaultSettings);
                });
    }

    @Transactional
    public SettingsEntity updateSettings(SettingsDto dto) {
        SettingsEntity settings = getSettings();
        
        if (dto.getDataRetentionDays() != null) settings.setDataRetentionDays(dto.getDataRetentionDays());
        if (dto.getIncidentRetentionDays() != null) settings.setIncidentRetentionDays(dto.getIncidentRetentionDays());
        if (dto.getAnomalySensitivity() != null) settings.setAnomalySensitivity(dto.getAnomalySensitivity());
        if (dto.getAutoBanEnabled() != null) settings.setAutoBanEnabled(dto.getAutoBanEnabled());
        if (dto.getRateLimitSuspicious() != null) settings.setRateLimitSuspicious(dto.getRateLimitSuspicious());
        if (dto.getAlertsEmail() != null) settings.setAlertsEmail(dto.getAlertsEmail());
        if (dto.getSlackWebhookUrl() != null) settings.setSlackWebhookUrl(dto.getSlackWebhookUrl());
        if (dto.getSlackAlertsEnabled() != null) settings.setSlackAlertsEnabled(dto.getSlackAlertsEnabled());

        return settingsRepository.save(settings);
    }
}
