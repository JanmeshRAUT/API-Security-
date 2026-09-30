package com.apisecurity.starter.feature.state;

import java.util.List;
import java.util.Set;

public interface BehaviorStateStore {

    /**
     * Increment and get the count of requests for a specific key within the current time window.
     */
    int incrementAndGetRequestCount(String key);

    /**
     * Increment and get the count of failed requests for a specific key within the current time window.
     */
    int incrementAndGetFailedCount(String key);

    /**
     * Get the current count of failed requests for a specific key.
     */
    int getFailedCount(String key);

    /**
     * Increment and get the count of successful requests for a specific key within the current time window.
     */
    int incrementAndGetSuccessCount(String key);

    /**
     * Get the current count of successful requests for a specific key.
     */
    int getSuccessCount(String key);

    /**
     * Add an item to a unique set for a specific key and return the new size.
     */
    int addToUniqueSetAndGetSize(String key, String item);
    
    /**
     * Get the current size of a unique set for a specific key.
     */
    int getUniqueSetSize(String key);

    /**
     * Get the set of unique items for a specific key.
     */
    Set<String> getUniqueSet(String key);

    /**
     * Record object access for an identity and return true if sequential access pattern is detected.
     */
    boolean recordObjectAccessAndCheckSequential(String identityKey, String objectId);

    /**
     * Get recent accessed object IDs for an identity.
     */
    List<String> getAccessedObjectIds(String identityKey);
}
