package com.apisecurity.platform.repository;

import com.apisecurity.platform.domain.settings.SettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<SettingsEntity, String> {
}
