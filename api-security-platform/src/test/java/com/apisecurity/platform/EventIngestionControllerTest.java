package com.apisecurity.platform;

import com.apisecurity.platform.client.AiDetectionClient;
import com.apisecurity.platform.dto.ApplicationRegistrationRequest;
import com.apisecurity.platform.dto.ApplicationRegistrationResponse;
import com.apisecurity.platform.dto.DetectionResultDto;
import com.apisecurity.platform.dto.EventIngestionRequest;
import com.apisecurity.platform.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationService applicationService;

    @MockBean
    private AiDetectionClient aiDetectionClient;

    private String apiKey;
    private String appId;

    @BeforeEach
    public void setup() {
        appId = "app-ingest-" + UUID.randomUUID().toString().substring(0, 8);
        ApplicationRegistrationRequest req = new ApplicationRegistrationRequest();
        req.setName("Ingest App");
        req.setApplicationId(appId);
        ApplicationRegistrationResponse res = applicationService.registerApplication(req);
        apiKey = res.getApiKey();
    }

    @Test
    public void testEventIngestionWithValidApiKeyAndThreatDetected() throws Exception {
        DetectionResultDto aiResult = DetectionResultDto.builder()
                .eventId("evt-100")
                .detected(true)
                .threatType("CREDENTIAL_STUFFING")
                .confidence(0.95)
                .riskScore(0.92)
                .severity("CRITICAL")
                .recommendedAction("BLOCK")
                .reasonCodes(List.of("HIGH_FAILURE_RATIO", "DISTRIBUTED_LOGIN_PATTERN"))
                .build();

        Mockito.when(aiDetectionClient.detectThreat(any())).thenReturn(Optional.of(aiResult));

        EventIngestionRequest eventReq = EventIngestionRequest.builder()
                .eventId("evt-100")
                .applicationId(appId)
                .requestFeatures(EventIngestionRequest.RequestFeatures.builder()
                        .method("POST").endpoint("/api/auth/login").statusCode(401).responseTimeMs(100).build())
                .behaviorFeatures(EventIngestionRequest.BehaviorFeatures.builder()
                        .requestFrequency(50).failedRequestCount(45).uniqueUsers(20).uniqueSourceIps(10).build())
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId", is("evt-100")))
                .andExpect(jsonPath("$.detected", is(true)))
                .andExpect(jsonPath("$.threatType", is("CREDENTIAL_STUFFING")))
                .andExpect(jsonPath("$.severity", is("CRITICAL")));
    }

    @Test
    public void testEventIngestionInvalidApiKey() throws Exception {
        EventIngestionRequest eventReq = EventIngestionRequest.builder()
                .eventId("evt-invalid-key")
                .applicationId(appId)
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", "sec_invalid_key_hash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testEventIngestionAiServiceUnavailable() throws Exception {
        Mockito.when(aiDetectionClient.detectThreat(any())).thenReturn(Optional.empty());

        EventIngestionRequest eventReq = EventIngestionRequest.builder()
                .eventId("evt-ai-down")
                .applicationId(appId)
                .requestFeatures(EventIngestionRequest.RequestFeatures.builder()
                        .method("GET").endpoint("/api/orders/1").statusCode(200).build())
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detected", is(false)))
                .andExpect(jsonPath("$.reasonCodes", hasItem("AI_DETECTION_SERVICE_UNAVAILABLE")));
    }
}
