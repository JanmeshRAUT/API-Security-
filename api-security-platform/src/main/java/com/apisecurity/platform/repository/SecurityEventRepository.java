package com.apisecurity.platform.repository;

import com.apisecurity.platform.domain.event.ProcessingStatus;
import com.apisecurity.platform.domain.event.SecurityEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEventEntity, String>, JpaSpecificationExecutor<SecurityEventEntity> {
    Optional<SecurityEventEntity> findByEventId(String eventId);
    Page<SecurityEventEntity> findByApplicationId(String applicationId, Pageable pageable);
    List<SecurityEventEntity> findByProcessingStatus(ProcessingStatus status);
    List<SecurityEventEntity> findTop1000ByOrderByTimestampDesc();
    List<SecurityEventEntity> findTop1000ByApplicationIdOrderByTimestampDesc(String applicationId);
}
