package com.apisecurity.platform.client;

import com.apisecurity.platform.dto.DetectionResultDto;
import com.apisecurity.platform.dto.EventIngestionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class AiDetectionClient {

    private static final Logger log = LoggerFactory.getLogger(AiDetectionClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public AiDetectionClient(
            @Value("${api.security.ai.base-url:http://localhost:8000}") String baseUrl,
            @Value("${api.security.ai.api-key:development-secret}") String apiKey,
            @Value("${api.security.ai.connect-timeout-ms:1000}") int connectTimeoutMs,
            @Value("${api.security.ai.read-timeout-ms:2000}") int readTimeoutMs) {

        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-API-Key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Submits SecurityFeatureSet event payload to FastAPI Central Detection API (POST /api/v1/detect).
     * Returns DetectionResultDto if successful, or empty Optional if AI detection service is unavailable.
     */
    public Optional<DetectionResultDto> detectThreat(EventIngestionRequest request) {
        try {
            log.debug("Sending event [{}] to AI Detection Service", request.getEventId());

            DetectionResultDto result = restClient.post()
                    .uri("/api/v1/detect")
                    .body(request)
                    .retrieve()
                    .body(DetectionResultDto.class);

            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("AI Detection Service call failed for event [{}]: {}", request.getEventId(), e.getMessage());
            return Optional.empty();
        }
    }
}
