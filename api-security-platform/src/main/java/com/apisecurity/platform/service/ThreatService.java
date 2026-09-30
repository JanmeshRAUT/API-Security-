package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.threat.Severity;
import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.domain.threat.ThreatStatus;
import com.apisecurity.platform.domain.threat.ThreatType;
import com.apisecurity.platform.dto.ThreatResponseDto;
import com.apisecurity.platform.exception.ResourceNotFoundException;
import com.apisecurity.platform.repository.ThreatRepository;
import com.apisecurity.platform.publisher.RealTimeSecurityPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.apisecurity.platform.mapper.ThreatMapper;

@Service
public class ThreatService {

    private final ThreatRepository threatRepository;
    private final RealTimeSecurityPublisher realTimePublisher;
    private final ThreatMapper threatMapper;

    public ThreatService(
            ThreatRepository threatRepository,
            RealTimeSecurityPublisher realTimePublisher,
            ThreatMapper threatMapper) {
        this.threatRepository = threatRepository;
        this.realTimePublisher = realTimePublisher;
        this.threatMapper = threatMapper;
    }

    @Transactional(readOnly = true)
    public Page<ThreatResponseDto> getThreats(
            String applicationId,
            ThreatType threatType,
            Severity severity,
            ThreatStatus status,
            String endpoint,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ThreatEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (applicationId != null && !applicationId.isBlank()) {
                predicates.add(cb.equal(root.get("applicationId"), applicationId));
            }
            if (threatType != null) {
                predicates.add(cb.equal(root.get("threatType"), threatType));
            }
            if (severity != null) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (endpoint != null && !endpoint.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("endpoint")), "%" + endpoint.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return threatRepository.findAll(spec, pageable).map(threatMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<ThreatResponseDto> getThreatsByApplication(String applicationId, Pageable pageable) {
        return threatRepository.findByApplicationId(applicationId, pageable).map(threatMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public ThreatResponseDto getThreatById(String id) {
        ThreatEntity entity = threatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat not found with id: " + id));
        return threatMapper.toResponseDto(entity);
    }

    @Transactional
    public ThreatResponseDto updateThreatStatus(String id, ThreatStatus status) {
        ThreatEntity entity = threatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Threat not found with id: " + id));
        entity.setStatus(status);
        ThreatEntity updated = threatRepository.save(entity);

        // Broadcast THREAT_UPDATED over WebSockets
        realTimePublisher.publishThreatUpdate(updated);

        return threatMapper.toResponseDto(updated);
    }
}
