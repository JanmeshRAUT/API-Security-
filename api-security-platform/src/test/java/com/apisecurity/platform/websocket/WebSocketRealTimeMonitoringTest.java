package com.apisecurity.platform.websocket;

import com.apisecurity.platform.client.AiDetectionClient;
import com.apisecurity.platform.domain.threat.ThreatStatus;
import com.apisecurity.platform.dto.*;
import com.apisecurity.platform.dto.realtime.RealtimeMessageType;
import com.apisecurity.platform.dto.realtime.RealtimeSecurityMessage;
import com.apisecurity.platform.service.ApplicationService;
import com.apisecurity.platform.service.EventIngestionService;
import com.apisecurity.platform.service.ThreatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketRealTimeMonitoringTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventIngestionService eventIngestionService;

    @Autowired
    private ThreatService threatService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiDetectionClient aiDetectionClient;

    private WebSocketStompClient stompClient;
    private String apiKey;
    private String appId;

    @BeforeEach
    public void setup() {
        appId = "ws-app-" + UUID.randomUUID().toString().substring(0, 8);
        ApplicationRegistrationRequest req = new ApplicationRegistrationRequest();
        req.setName("WebSocket Test App");
        req.setApplicationId(appId);
        ApplicationRegistrationResponse res = applicationService.registerApplication(req);
        apiKey = res.getApiKey();

        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(converter);
    }

    @Test
    public void testWebSocketEventAndThreatBroadcasting() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws/security";

        CompletableFuture<RealtimeSecurityMessage> eventFuture = new CompletableFuture<>();
        CompletableFuture<RealtimeSecurityMessage> threatFuture = new CompletableFuture<>();

        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/security/events", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return RealtimeSecurityMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        eventFuture.complete((RealtimeSecurityMessage) payload);
                    }
                });

                session.subscribe("/topic/security/threats", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return RealtimeSecurityMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        threatFuture.complete((RealtimeSecurityMessage) payload);
                    }
                });
            }
        };

        StompSession session = stompClient.connectAsync(wsUrl, sessionHandler).get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());
        Thread.sleep(500); // Allow STOMP subscription registration in broker

        // Mock AI Detection result with Threat detected
        DetectionResultDto aiResult = DetectionResultDto.builder()
                .eventId("evt-ws-1")
                .detected(true)
                .threatType("CREDENTIAL_STUFFING")
                .confidence(0.95)
                .riskScore(0.91)
                .severity("CRITICAL")
                .recommendedAction("BLOCK")
                .reasonCodes(List.of("HIGH_FAILURE_RATIO"))
                .build();

        Mockito.when(aiDetectionClient.detectThreat(any())).thenReturn(Optional.of(aiResult));

        EventIngestionRequest eventReq = EventIngestionRequest.builder()
                .eventId("evt-ws-1")
                .applicationId(appId)
                .requestFeatures(EventIngestionRequest.RequestFeatures.builder()
                        .method("POST").endpoint("/api/auth/login").statusCode(401).build())
                .behaviorFeatures(EventIngestionRequest.BehaviorFeatures.builder()
                        .failedRequestCount(10).requestFrequency(30).build())
                .build();

        // Ingest event -> triggers publishEvent & publishThreat
        eventIngestionService.ingestAndAnalyzeEvent(eventReq, apiKey);

        RealtimeSecurityMessage eventMsg = eventFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(eventMsg);
        assertEquals(RealtimeMessageType.API_EVENT, eventMsg.getType());

        RealtimeSecurityMessage threatMsg = threatFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(threatMsg);
        assertEquals(RealtimeMessageType.THREAT_DETECTED, threatMsg.getType());
    }

    @Test
    public void testWebSocketThreatStatusUpdateBroadcasting() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/ws/security";
        CompletableFuture<RealtimeSecurityMessage> updateFuture = new CompletableFuture<>();

        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/security/threats", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return RealtimeSecurityMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        RealtimeSecurityMessage msg = (RealtimeSecurityMessage) payload;
                        if (msg.getType() == RealtimeMessageType.THREAT_UPDATED) {
                            updateFuture.complete(msg);
                        }
                    }
                });
            }
        };

        StompSession session = stompClient.connectAsync(wsUrl, sessionHandler).get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());
        Thread.sleep(500);

        DetectionResultDto aiResult = DetectionResultDto.builder()
                .eventId("evt-ws-2")
                .detected(true)
                .threatType("BOLA")
                .confidence(0.90)
                .riskScore(0.85)
                .severity("HIGH")
                .recommendedAction("ALERT")
                .reasonCodes(List.of("SEQUENTIAL_OBJECT_ACCESS"))
                .build();

        Mockito.when(aiDetectionClient.detectThreat(any())).thenReturn(Optional.of(aiResult));

        EventIngestionRequest eventReq = EventIngestionRequest.builder()
                .eventId("evt-ws-2")
                .applicationId(appId)
                .requestFeatures(EventIngestionRequest.RequestFeatures.builder()
                        .method("GET").endpoint("/api/orders/101").statusCode(200).build())
                .build();

        eventIngestionService.ingestAndAnalyzeEvent(eventReq, apiKey);

        // Fetch threat ID created for evt-ws-2
        var threatsPage = threatService.getThreats(appId, null, null, null, null, 0, 10, "detectedAt", "desc");
        assertEquals(1, threatsPage.getTotalElements());
        String threatId = threatsPage.getContent().get(0).getId();

        // Update threat status to ACKNOWLEDGED -> should broadcast THREAT_UPDATED
        threatService.updateThreatStatus(threatId, ThreatStatus.ACKNOWLEDGED);

        RealtimeSecurityMessage updateMsg = updateFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(updateMsg);
        assertEquals(RealtimeMessageType.THREAT_UPDATED, updateMsg.getType());
    }
}
