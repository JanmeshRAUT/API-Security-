package com.apisecurity.starter.client;

import com.apisecurity.starter.event.ApiSecurityEvent;

public interface SecurityPlatformClient {
    
    /**
     * Sends the security event to the central platform.
     * Must be non-blocking or handle failures gracefully.
     *
     * @param event The security event to send
     */
    void sendEvent(ApiSecurityEvent event);
}
