package com.apisecurity.platform.controller;

import com.apisecurity.platform.dto.*;
import com.apisecurity.platform.service.ApplicationService;
import com.apisecurity.platform.service.EventIngestionService;
import com.apisecurity.platform.service.ThreatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final EventIngestionService eventIngestionService;
    private final ThreatService threatService;

    public ApplicationController(
            ApplicationService applicationService,
            EventIngestionService eventIngestionService,
            ThreatService threatService) {
        this.applicationService = applicationService;
        this.eventIngestionService = eventIngestionService;
        this.threatService = threatService;
    }

    @PostMapping
    public ResponseEntity<ApplicationRegistrationResponse> registerApplication(
            @Valid @RequestBody ApplicationRegistrationRequest request) {
        ApplicationRegistrationResponse response = applicationService.registerApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable String id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate-key")
    public ResponseEntity<ApplicationRegistrationResponse> rotateApiKey(@PathVariable String id) {
        ApplicationRegistrationResponse response = applicationService.rotateApiKey(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<Page<EventResponseDto>> getApplicationEvents(
            @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable) {
        ApplicationResponse app = applicationService.getApplicationById(id);
        return ResponseEntity.ok(eventIngestionService.getEventsByApplication(app.getApplicationId(), pageable));
    }

    @GetMapping("/{id}/threats")
    public ResponseEntity<Page<ThreatResponseDto>> getApplicationThreats(
            @PathVariable String id,
            @PageableDefault(size = 20) Pageable pageable) {
        ApplicationResponse app = applicationService.getApplicationById(id);
        return ResponseEntity.ok(threatService.getThreatsByApplication(app.getApplicationId(), pageable));
    }
}
