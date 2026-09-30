package com.apisecurity.starter.handler;

public interface SecurityResponseHandler {

    enum Decision {
        ALLOW, ALERT, BLOCK
    }

    /**
     * Handles the decision returned from the security platform.
     *
     * @param decision The decision made by the AI models
     */
    void handleDecision(Decision decision);
}
