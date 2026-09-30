package com.apisecurity.starter.feature;

public interface FeaturePublisher {
    
    /**
     * Publishes the fully constructed ML-ready feature set to the central platform.
     *
     * @param featureSet The structured security feature set
     */
    void publish(SecurityFeatureSet featureSet);
}
