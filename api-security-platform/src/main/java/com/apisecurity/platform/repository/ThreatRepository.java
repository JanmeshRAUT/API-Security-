package com.apisecurity.platform.repository;

import com.apisecurity.platform.domain.threat.Severity;
import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.domain.threat.ThreatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreatRepository extends JpaRepository<ThreatEntity, String>, JpaSpecificationExecutor<ThreatEntity> {
    long countBySeverity(Severity severity);
    long countByThreatType(ThreatType threatType);
    Page<ThreatEntity> findByApplicationId(String applicationId, Pageable pageable);
}
