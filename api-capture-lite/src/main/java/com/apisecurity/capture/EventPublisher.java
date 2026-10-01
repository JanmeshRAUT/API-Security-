package com.apisecurity.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EventPublisher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final ApiCaptureProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ThreadPoolExecutor executor;

    private final AtomicLong capturedCount = new AtomicLong(0);
    private final AtomicLong sentCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);
    private final AtomicLong droppedCount = new AtomicLong(0);

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitOpenUntil = 0;

    private volatile long lastWarnTime = 0;

    public EventPublisher(ApiCaptureProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getPublisher().getConnectTimeoutMs()))
                .build();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "api-capture-" + counter.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };

        this.executor = new ThreadPoolExecutor(
                properties.getPublisher().getWorkerThreads(),
                properties.getPublisher().getWorkerThreads(),
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.getPublisher().getQueueCapacity()),
                threadFactory,
                (r, executor) -> droppedCount.incrementAndGet()
        );
    }

    public void publish(ApiEvent event) {
        capturedCount.incrementAndGet();

        if (System.currentTimeMillis() < circuitOpenUntil) {
            droppedCount.incrementAndGet();
            return;
        }

        executor.execute(() -> {
            try {
                String json = objectMapper.writeValueAsString(event);
                
                String targetUrl = properties.getPlatform().getBaseUrl() + properties.getPlatform().getPath();
                
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofMillis(properties.getPublisher().getRequestTimeoutMs()))
                        .POST(HttpRequest.BodyPublishers.ofString(json));
                        
                if (properties.getPlatform().getApiKey() != null && !properties.getPlatform().getApiKey().isBlank()) {
                    requestBuilder.header("X-API-Key", properties.getPlatform().getApiKey());
                }

                HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 400) {
                    handleFailure("Platform returned status " + response.statusCode());
                } else {
                    sentCount.incrementAndGet();
                    consecutiveFailures.set(0);
                    circuitOpenUntil = 0;
                }
            } catch (Exception e) {
                handleFailure(e.getMessage());
            }
        });
    }

    private void handleFailure(String message) {
        failedCount.incrementAndGet();
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= 5) {
            circuitOpenUntil = System.currentTimeMillis() + 30_000;
            rateLimitedWarn("Circuit breaker opened due to 5 consecutive failures. Last error: " + message);
        } else {
            rateLimitedWarn("Failed to send API event to platform: " + message);
        }
    }

    private void rateLimitedWarn(String message) {
        long now = System.currentTimeMillis();
        if (now - lastWarnTime > 30_000) {
            lastWarnTime = now;
            log.warn(message);
        }
    }

    public long getCaptured() { return capturedCount.get(); }
    public long getSent() { return sentCount.get(); }
    public long getFailed() { return failedCount.get(); }
    public long getDropped() { return droppedCount.get(); }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
