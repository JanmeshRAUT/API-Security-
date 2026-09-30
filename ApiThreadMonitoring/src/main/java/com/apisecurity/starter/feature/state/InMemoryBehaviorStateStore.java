package com.apisecurity.starter.feature.state;

import com.apisecurity.starter.config.ApiSecurityProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A bounded, time-windowed in-memory state store for behavioral security telemetry.
 */
public class InMemoryBehaviorStateStore implements BehaviorStateStore {

    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> failedCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> successCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> uniqueSets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> objectAccessHistory = new ConcurrentHashMap<>();

    private final long windowSizeMs;
    private final int maxTrackedEntries;
    private volatile long currentBucketTime;

    public InMemoryBehaviorStateStore() {
        this(300, 1000);
    }

    public InMemoryBehaviorStateStore(int windowDurationSeconds, int maxTrackedEntries) {
        this.windowSizeMs = Math.max(1, windowDurationSeconds) * 1000L;
        this.maxTrackedEntries = maxTrackedEntries;
        this.currentBucketTime = getCurrentBucket();
    }

    public InMemoryBehaviorStateStore(ApiSecurityProperties properties) {
        this(
            properties.getBehavior() != null ? properties.getBehavior().getWindowDurationSeconds() : 300,
            properties.getBehavior() != null ? properties.getBehavior().getMaxTrackedUsers() : 1000
        );
    }

    private long getCurrentBucket() {
        return System.currentTimeMillis() / windowSizeMs;
    }

    private void checkAndResetWindow() {
        long bucket = getCurrentBucket();
        if (bucket != currentBucketTime) {
            synchronized (this) {
                if (bucket != currentBucketTime) {
                    requestCounts.clear();
                    failedCounts.clear();
                    successCounts.clear();
                    uniqueSets.clear();
                    objectAccessHistory.clear();
                    currentBucketTime = bucket;
                }
            }
        }
    }

    private void enforceBounds(ConcurrentHashMap<?, ?> map) {
        if (map.size() > maxTrackedEntries) {
            synchronized (this) {
                if (map.size() > maxTrackedEntries) {
                    map.clear();
                }
            }
        }
    }

    @Override
    public int incrementAndGetRequestCount(String key) {
        checkAndResetWindow();
        enforceBounds(requestCounts);
        return requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public int incrementAndGetFailedCount(String key) {
        checkAndResetWindow();
        enforceBounds(failedCounts);
        return failedCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public int getFailedCount(String key) {
        checkAndResetWindow();
        AtomicInteger count = failedCounts.get(key);
        return count != null ? count.get() : 0;
    }

    @Override
    public int incrementAndGetSuccessCount(String key) {
        checkAndResetWindow();
        enforceBounds(successCounts);
        return successCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public int getSuccessCount(String key) {
        checkAndResetWindow();
        AtomicInteger count = successCounts.get(key);
        return count != null ? count.get() : 0;
    }

    @Override
    public int addToUniqueSetAndGetSize(String key, String item) {
        checkAndResetWindow();
        enforceBounds(uniqueSets);
        Set<String> set = uniqueSets.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        set.add(item);
        return set.size();
    }

    @Override
    public int getUniqueSetSize(String key) {
        checkAndResetWindow();
        Set<String> set = uniqueSets.get(key);
        return set != null ? set.size() : 0;
    }

    @Override
    public Set<String> getUniqueSet(String key) {
        checkAndResetWindow();
        Set<String> set = uniqueSets.get(key);
        return set != null ? Collections.unmodifiableSet(new LinkedHashSet<>(set)) : Collections.emptySet();
    }

    @Override
    public boolean recordObjectAccessAndCheckSequential(String identityKey, String objectId) {
        checkAndResetWindow();
        if (objectId == null || objectId.isBlank()) {
            return false;
        }

        enforceBounds(objectAccessHistory);
        List<String> history = objectAccessHistory.computeIfAbsent(identityKey, k -> new CopyOnWriteArrayList<>());
        history.add(objectId);
        if (history.size() > 50) {
            history.remove(0);
        }

        addToUniqueSetAndGetSize("object_ids_" + identityKey, objectId);
        addToUniqueSetAndGetSize("all_object_ids", objectId);

        return isSequentialPattern(history);
    }

    @Override
    public List<String> getAccessedObjectIds(String identityKey) {
        checkAndResetWindow();
        List<String> history = objectAccessHistory.get(identityKey);
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    private boolean isSequentialPattern(List<String> history) {
        if (history.size() < 3) {
            return false;
        }

        List<Long> numericIds = new ArrayList<>();
        for (String item : history) {
            try {
                numericIds.add(Long.parseLong(item.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        if (numericIds.size() < 3) {
            return false;
        }

        // Check if any window of 3 or more consecutive IDs in numericIds has diff of +1 or -1
        int consecutiveCount = 1;
        for (int i = 1; i < numericIds.size(); i++) {
            long diff = numericIds.get(i) - numericIds.get(i - 1);
            if (diff == 1 || diff == -1) {
                consecutiveCount++;
                if (consecutiveCount >= 3) {
                    return true;
                }
            } else {
                consecutiveCount = 1;
            }
        }

        return false;
    }
}
