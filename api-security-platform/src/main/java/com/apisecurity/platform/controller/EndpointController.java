package com.apisecurity.platform.controller;

import com.apisecurity.platform.dto.ApiMetricsDto;
import com.apisecurity.platform.dto.EndpointSummaryDto;
import com.apisecurity.platform.service.EndpointCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/endpoints")
public class EndpointController {

    private final EndpointCatalogService endpointCatalogService;

    public EndpointController(EndpointCatalogService endpointCatalogService) {
        this.endpointCatalogService = endpointCatalogService;
    }

    @GetMapping
    public ResponseEntity<List<EndpointSummaryDto>> getEndpoints(
            @RequestParam(required = false) String applicationId) {
        return ResponseEntity.ok(endpointCatalogService.getEndpoints(applicationId));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiMetricsDto> getMetrics(
            @RequestParam(required = false) String applicationId) {
        return ResponseEntity.ok(endpointCatalogService.getMetrics(applicationId));
    }
}
