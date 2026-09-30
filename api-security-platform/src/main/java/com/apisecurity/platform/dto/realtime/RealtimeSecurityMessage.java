package com.apisecurity.platform.dto.realtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeSecurityMessage<T> {

    private RealtimeMessageType type;
    private LocalDateTime timestamp;
    private T payload;

    public static <T> RealtimeSecurityMessage<T> of(RealtimeMessageType type, T payload) {
        return RealtimeSecurityMessage.<T>builder()
                .type(type)
                .timestamp(LocalDateTime.now())
                .payload(payload)
                .build();
    }

    public T getEventPayload() {
        return type == RealtimeMessageType.API_EVENT ? payload : null;
    }

    public T getThreatPayload() {
        return (type == RealtimeMessageType.THREAT_DETECTED || type == RealtimeMessageType.THREAT_UPDATED) ? payload : null;
    }
}
