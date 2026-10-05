# API Capture Lite

`api-capture-lite` is a Spring Boot auto-configured servlet filter that captures API traffic asynchronously and posts it to the API Security Platform for analysis.

## Features

- **Asynchronous**: Events are published in a background thread pool with a bounded queue. Host application response times are unaffected.
- **Circuit Breaking & Timeouts**: Fails safely. If the platform is down, events are dropped rather than exhausting host application threads.
- **Lightweight**: Zero external dependencies other than Spring Boot and Jackson.
- **Privacy-First**: Captures only metadata (method, path, IP, size, timing). **Never** captures headers (except User-Agent optionally), cookies, query strings, authentication tokens, or request/response bodies.

## Installation

```xml
<dependency>
    <groupId>com.apisecurity</groupId>
    <artifactId>api-capture-lite</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

Add the following to your `application.yml`:

```yaml
api:
  capture:
    enabled: true
    applicationId: "my-ecom-app" # defaults to spring.application.name
    platform:
      baseUrl: "http://localhost:8085"
      path: "/api/v1/events"
      apiKey: "your-api-key-here"
    excludedPaths:
      - "/actuator/**"
      - "/swagger-ui/**"
      - "/v3/api-docs/**"
      - "/favicon.ico"
      - "/error"
    trustedProxies:
      - "10.0.0.1" # list exact proxies to prevent spoofing
    captureUserAgent: true
    filterOrder: -2147483638 # Ordered.HIGHEST_PRECEDENCE + 10
```

### Filter Order Trade-off
By default, the filter order is set to run *before* Spring Security (highest precedence + 10). 
- **Pros**: Captures 401 Unauthorized and 403 Forbidden requests rejected by Spring Security.
- **Cons**: `request.getUserPrincipal()` might be null for unauthenticated or rejected requests.

If you need the authenticated principal for all captured events, set `api.capture.filterOrder` to a higher number (lower precedence) so it runs *after* the Spring Security filter chain. Note that the user ID is null on Spring Security apps unless you supply a custom `UserIdResolver` bean or ensure the filter runs after authentication.

## JSON Payload Shape

```json
{
  "eventId": "uuid",
  "applicationId": "my-ecom-app",
  "timestamp": "2024-03-20T10:00:00Z",
  "clientIp": "192.168.1.100",
  "request": {
    "method": "GET",
    "endpoint": "/orders/{id}",
    "rawUri": "/orders/1001",
    "sourceIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "requestSize": 0,
    "pathDepth": 2
  },
  "response": {
    "statusCode": 200,
    "responseTimeMs": 42,
    "responseSize": 1024
  },
  "identity": {
    "authenticated": true,
    "userId": "alice"
  }
}
```

## Smoke Test

1. Start the API Security Platform.
2. Register an application via `POST /api/v1/applications` to get an API Key.
3. Configure your host application with the API Key (`api.capture.platform.apiKey`).
4. Make a request to an endpoint on your host application.
5. Verify the event appears in the platform by calling `GET /api/v1/events?applicationId=<id>`.
