package com.apisecurity.capture;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventPublisher {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // URL of your api-security-platform ingestion endpoint
    private final String ingestionUrl = "http://localhost:8080/api/ingest"; 

    public void publish(ApiEvent event) {
        // Run in a background thread so we don't slow down the main API response
        executor.submit(() -> {
            try {
                String json = objectMapper.writeValueAsString(event);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ingestionUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                        
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception e) {
                // Silently fail or log if the platform is down. We should never crash the host app.
                System.err.println("Failed to send API event to platform: " + e.getMessage());
            }
        });
    }
}
