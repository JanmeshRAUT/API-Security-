package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.threat.Severity;
import com.apisecurity.platform.domain.threat.ThreatType;
import com.apisecurity.platform.dto.DashboardSummaryDto;
import com.apisecurity.platform.repository.ApplicationRepository;
import com.apisecurity.platform.repository.SecurityEventRepository;
import com.apisecurity.platform.repository.ThreatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final SecurityEventRepository eventRepository;
    private final ThreatRepository threatRepository;

    public DashboardService(
            ApplicationRepository applicationRepository,
            SecurityEventRepository eventRepository,
            ThreatRepository threatRepository) {
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
        this.threatRepository = threatRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        long totalApps = applicationRepository.count();
        long totalEvents = eventRepository.count();
        long totalThreats = threatRepository.count();

        long criticalThreats = threatRepository.countBySeverity(Severity.CRITICAL);
        long highThreats = threatRepository.countBySeverity(Severity.HIGH);

        long csThreats = threatRepository.countByThreatType(ThreatType.CREDENTIAL_STUFFING);
        long bolaThreats = threatRepository.countByThreatType(ThreatType.BOLA);
        long idEnumThreats = threatRepository.countByThreatType(ThreatType.ID_ENUMERATION);

        Map<String, Long> threatsByType = new HashMap<>();
        for (ThreatType type : ThreatType.values()) {
            threatsByType.put(type.name(), threatRepository.countByThreatType(type));
        }

        Map<String, Long> threatsBySeverity = new HashMap<>();
        for (Severity sev : Severity.values()) {
            threatsBySeverity.put(sev.name(), threatRepository.countBySeverity(sev));
        }

        return DashboardSummaryDto.builder()
                .totalApplications(totalApps)
                .totalEvents(totalEvents)
                .totalThreats(totalThreats)
                .criticalThreats(criticalThreats)
                .highThreats(highThreats)
                .credentialStuffingThreats(csThreats)
                .bolaThreats(bolaThreats)
                .idEnumerationThreats(idEnumThreats)
                .threatsByType(threatsByType)
                .threatsBySeverity(threatsBySeverity)
                .build();
    }
}
