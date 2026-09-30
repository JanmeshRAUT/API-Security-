package com.apisecurity.platform.publisher;

import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.dto.realtime.RealtimeMessageType;
import com.apisecurity.platform.dto.realtime.RealtimeSecurityEventDto;
import com.apisecurity.platform.dto.realtime.RealtimeSecurityMessage;
import com.apisecurity.platform.dto.realtime.RealtimeThreatDto;
import com.apisecurity.platform.mapper.SecurityEventMapper;
import com.apisecurity.platform.mapper.ThreatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketSecurityPublisher implements RealTimeSecurityPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSecurityPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SecurityEventMapper securityEventMapper;
    private final ThreatMapper threatMapper;

    public WebSocketSecurityPublisher(
            SimpMessagingTemplate messagingTemplate,
            SecurityEventMapper securityEventMapper,
            ThreatMapper threatMapper) {
        this.messagingTemplate = messagingTemplate;
        this.securityEventMapper = securityEventMapper;
        this.threatMapper = threatMapper;
    }

    @Override
    public void publishEvent(SecurityEventEntity event) {
        try {
            RealtimeSecurityEventDto dto = securityEventMapper.toRealtimeDto(event);
            RealtimeSecurityMessage<RealtimeSecurityEventDto> message =
                    RealtimeSecurityMessage.of(RealtimeMessageType.API_EVENT, dto);

            // Publish to global events topic
            messagingTemplate.convertAndSend("/topic/security/events", message);

            // Publish to app-specific topic
            messagingTemplate.convertAndSend("/topic/security/applications/" + event.getApplicationId(), message);

            log.debug("Broadcasted API event [{}] to WebSocket subscribers", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to broadcast security event [{}] over WebSocket: {}", event.getEventId(), e.getMessage());
        }
    }

    @Override
    public void publishThreat(ThreatEntity threat) {
        try {
            RealtimeThreatDto dto = threatMapper.toRealtimeDto(threat);
            RealtimeSecurityMessage<RealtimeThreatDto> message =
                    RealtimeSecurityMessage.of(RealtimeMessageType.THREAT_DETECTED, dto);

            // Publish to global threats topic
            messagingTemplate.convertAndSend("/topic/security/threats", message);

            // Publish to app-specific topic
            messagingTemplate.convertAndSend("/topic/security/applications/" + threat.getApplicationId(), message);

            log.info("Broadcasted THREAT_DETECTED [{}] to WebSocket subscribers", threat.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast threat alert [{}] over WebSocket: {}", threat.getId(), e.getMessage());
        }
    }

    @Override
    public void publishThreatUpdate(ThreatEntity threat) {
        try {
            RealtimeThreatDto dto = threatMapper.toRealtimeDto(threat);
            RealtimeSecurityMessage<RealtimeThreatDto> message =
                    RealtimeSecurityMessage.of(RealtimeMessageType.THREAT_UPDATED, dto);

            // Publish to global threats topic
            messagingTemplate.convertAndSend("/topic/security/threats", message);

            // Publish to app-specific topic
            messagingTemplate.convertAndSend("/topic/security/applications/" + threat.getApplicationId(), message);

            log.info("Broadcasted THREAT_UPDATED [{}] to WebSocket subscribers", threat.getId());
        } catch (Exception e) {
            log.error("Failed to broadcast threat update [{}] over WebSocket: {}", threat.getId(), e.getMessage());
        }
    }
}
