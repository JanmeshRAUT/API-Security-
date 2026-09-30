package com.apisecurity.platform.publisher;

import com.apisecurity.platform.domain.event.SecurityEventEntity;
import com.apisecurity.platform.domain.threat.ThreatEntity;
import com.apisecurity.platform.dto.realtime.RealtimeThreatDto;

public interface RealTimeSecurityPublisher {

    /**
     * Broadcasts newly ingested API security event.
     */
    void publishEvent(SecurityEventEntity event);

    /**
     * Broadcasts newly detected security threat alert.
     */
    void publishThreat(ThreatEntity threat);

    /**
     * Broadcasts threat status change (OPEN -> ACKNOWLEDGED -> RESOLVED).
     */
    void publishThreatUpdate(ThreatEntity threat);
}
