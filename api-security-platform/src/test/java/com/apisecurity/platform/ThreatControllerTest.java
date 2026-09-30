package com.apisecurity.platform;

import com.apisecurity.platform.domain.threat.*;
import com.apisecurity.platform.dto.ThreatStatusUpdateRequest;
import com.apisecurity.platform.repository.ThreatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ThreatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThreatRepository threatRepository;

    private ThreatEntity savedThreat;

    @BeforeEach
    public void setup() {
        threatRepository.deleteAll();
        ThreatEntity t = ThreatEntity.builder()
                .eventId("evt-test-" + UUID.randomUUID())
                .applicationId("shop-sphere")
                .threatType(ThreatType.CREDENTIAL_STUFFING)
                .confidence(0.95)
                .riskScore(0.88)
                .severity(Severity.HIGH)
                .recommendedAction(RecommendedAction.BLOCK)
                .status(ThreatStatus.OPEN)
                .reasonCodes("[\"HIGH_FAILURE_RATIO\"]")
                .detectedAt(LocalDateTime.now())
                .endpoint("/api/auth/login")
                .httpMethod("POST")
                .statusCode(401)
                .build();
        savedThreat = threatRepository.save(t);
    }

    @Test
    public void testGetThreatsWithFiltering() throws Exception {
        mockMvc.perform(get("/api/v1/threats")
                        .param("severity", "HIGH")
                        .param("threatType", "CREDENTIAL_STUFFING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(savedThreat.getId())))
                .andExpect(jsonPath("$.content[0].severity", is("HIGH")));
    }

    @Test
    public void testGetThreatById() throws Exception {
        mockMvc.perform(get("/api/v1/threats/" + savedThreat.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedThreat.getId())))
                .andExpect(jsonPath("$.threatType", is("CREDENTIAL_STUFFING")));
    }

    @Test
    public void testUpdateThreatStatus() throws Exception {
        ThreatStatusUpdateRequest req = new ThreatStatusUpdateRequest();
        req.setStatus(ThreatStatus.RESOLVED);

        mockMvc.perform(patch("/api/v1/threats/" + savedThreat.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED")));
    }
}
