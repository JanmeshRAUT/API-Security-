package com.apisecurity.platform.controller;

import com.apisecurity.platform.dto.DetectionResultDto;
import com.apisecurity.platform.dto.EventIngestionRequest;
import com.apisecurity.platform.dto.EventResponseDto;
import com.apisecurity.platform.service.EventIngestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;

    public EventIngestionController(EventIngestionService eventIngestionService) {
        this.eventIngestionService = eventIngestionService;
    }

    @PostMapping
    public ResponseEntity<DetectionResultDto> ingestEvent(
            @Valid @RequestBody EventIngestionRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        DetectionResultDto result = eventIngestionService.ingestAndAnalyzeEvent(request, apiKey);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getEvents(
            @RequestParam(required = false) String applicationId,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(eventIngestionService.getEvents(
                applicationId, httpMethod, endpoint, statusCode, page, size, sortBy, sortDir));
    }
}
