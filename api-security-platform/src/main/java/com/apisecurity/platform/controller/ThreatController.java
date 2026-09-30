package com.apisecurity.platform.controller;

import com.apisecurity.platform.domain.threat.Severity;
import com.apisecurity.platform.domain.threat.ThreatStatus;
import com.apisecurity.platform.domain.threat.ThreatType;
import com.apisecurity.platform.dto.ThreatResponseDto;
import com.apisecurity.platform.dto.ThreatStatusUpdateRequest;
import com.apisecurity.platform.service.ThreatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/threats")
public class ThreatController {

    private final ThreatService threatService;

    public ThreatController(ThreatService threatService) {
        this.threatService = threatService;
    }

    @GetMapping
    public ResponseEntity<Page<ThreatResponseDto>> getThreats(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) ThreatType threatType,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) ThreatStatus status,
            @RequestParam(required = false) String endpoint,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "detectedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<ThreatResponseDto> threats = threatService.getThreats(
                applicationId, threatType, severity, status, endpoint, page, size, sortBy, sortDir);
        return ResponseEntity.ok(threats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreatResponseDto> getThreatById(@PathVariable String id) {
        return ResponseEntity.ok(threatService.getThreatById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ThreatResponseDto> updateThreatStatus(
            @PathVariable String id,
            @Valid @RequestBody ThreatStatusUpdateRequest request) {
        return ResponseEntity.ok(threatService.updateThreatStatus(id, request.getStatus()));
    }
}
