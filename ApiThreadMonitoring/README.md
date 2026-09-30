# API Security Spring Boot Starter

An AI-Based API Threat Detection and Security Monitoring Framework starter for Spring Boot applications.

## Integration

To integrate this security framework into your Spring Boot application, follow these steps:

### 1. Add the Dependency

Add the starter to your host application's `pom.xml`:

```xml
<dependency>
    <groupId>com.apisecurity</groupId>
    <artifactId>api-security-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Configure Properties

In your host application's `application.yml` or `application.properties`, configure the API Security settings:

```yaml
api:
  security:
    enabled: true
    application-id: "shop-sphere"
    platform:
      base-url: "http://security-platform:8080"
      api-key: "your-secret-api-key"
    behavior:
      mode: MONITOR
    monitoring:
      capture-request-body: false
      capture-response-body: false
      capture-headers: false
```

### 3. Run Your Application

Start your Spring Boot application as usual. The framework will automatically initialize, register the `ApiSecurityFilter`, and start monitoring your API traffic.

You should see the following logs on startup:

```text
========================================
 API Security Framework
 Status: ENABLED
 Application ID: shop-sphere
 Monitoring: ACTIVE
 Mode: MONITOR
========================================
```

## Features

- **Non-invasive Integration:** Plugs into existing Spring Boot applications without code changes to your controllers or business logic.
- **Asynchronous Event Transmission:** Securely captures API traffic metadata and forwards it to the central platform without blocking your API responses.
- **Graceful Error Handling:** Framework errors are isolated and logged, ensuring the host application continues to function normally even if the central security platform is temporarily unavailable.
- **Configurable Modes:** Supports different operating modes (`MONITOR`, `ALERT`, `BLOCK`) for flexible deployment.
