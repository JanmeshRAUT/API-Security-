package com.apisecurity.platform.controller;

import com.apisecurity.platform.domain.settings.SettingsEntity;
import com.apisecurity.platform.dto.SettingsDto;
import com.apisecurity.platform.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<SettingsEntity> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PutMapping
    public ResponseEntity<SettingsEntity> updateSettings(@RequestBody SettingsDto dto) {
        return ResponseEntity.ok(settingsService.updateSettings(dto));
    }
}
