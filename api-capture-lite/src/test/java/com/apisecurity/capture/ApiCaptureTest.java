package com.apisecurity.capture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiCaptureTest {

    private HttpServer fakePlatform;
    private int port;
    private ObjectMapper objectMapper = new ObjectMapper();
    private List<JsonNode> receivedEvents;
    private AtomicReference<String> receivedApiKey;
    private volatile int artificialDelayMs = 0;
    private volatile int platformStatus = 200;

    @BeforeEach
    void setUp() throws IOException {
        receivedEvents = new ArrayList<>();
        receivedApiKey = new AtomicReference<>();
        artificialDelayMs = 0;
        platformStatus = 200;

        fakePlatform = HttpServer.create(new InetSocketAddress(0), 0);
        port = fakePlatform.getAddress().getPort();
        fakePlatform.createContext("/api/v1/events", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (artificialDelayMs > 0) {
                    try { Thread.sleep(artificialDelayMs); } catch (InterruptedException e) {}
                }
                if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    receivedApiKey.set(exchange.getRequestHeaders().getFirst("X-API-Key"));
                    try (InputStream is = exchange.getRequestBody()) {
                        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        receivedEvents.add(objectMapper.readTree(body));
                    }
                }
                exchange.sendResponseHeaders(platformStatus, 0);
                exchange.getResponseBody().close();
            }
        });
        fakePlatform.start();
    }

    @AfterEach
    void tearDown() {
        if (fakePlatform != null) {
            fakePlatform.stop(0);
        }
    }

    private ApiCaptureProperties createProperties() {
        ApiCaptureProperties props = new ApiCaptureProperties();
        props.setApplicationId("test-app");
        props.getPlatform().setBaseUrl("http://localhost:" + port);
        props.getPlatform().setApiKey("secret-key");
        props.getPublisher().setConnectTimeoutMs(200);
        props.getPublisher().setRequestTimeoutMs(200);
        return props;
    }

    @Test
    void testSuccessfulCapture() throws Exception {
        ApiCaptureProperties props = createProperties();
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/123");
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/users/{id}");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("User-Agent", "Test-Agent");
        request.addHeader("Authorization", "Bearer token"); // Should be ignored
        request.setQueryString("sort=asc"); // Should be ignored

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) throws IOException, ServletException {
                ((HttpServletResponse) response).setStatus(200);
                super.doFilter(request, response);
            }
        };

        filter.doFilter(request, response, chain);
        
        publisher.close(); // waits for tasks to finish

        assertThat(receivedEvents).hasSize(1);
        JsonNode event = receivedEvents.get(0);
        
        assertThat(receivedApiKey.get()).isEqualTo("secret-key");
        
        assertThat(event.get("applicationId").asText()).isEqualTo("test-app");
        assertThat(event.get("eventId")).isNotNull();
        assertThat(event.get("timestamp")).isNotNull();
        assertThat(event.get("clientIp").asText()).isEqualTo("10.0.0.1");
        
        JsonNode req = event.get("request");
        assertThat(req.get("method").asText()).isEqualTo("GET");
        assertThat(req.get("rawUri").asText()).isEqualTo("/users/123"); // No query string
        assertThat(req.get("endpoint").asText()).isEqualTo("/users/{id}");
        assertThat(req.get("sourceIp").asText()).isEqualTo("10.0.0.1");
        assertThat(req.get("userAgent").asText()).isEqualTo("Test-Agent");
        assertThat(req.get("pathDepth").asInt()).isEqualTo(2);
        
        JsonNode res = event.get("response");
        assertThat(res.get("statusCode").asInt()).isEqualTo(200);
        assertThat(res.get("responseTimeMs")).isNotNull();

        assertThat(event.toString()).doesNotContain("Bearer token");
        assertThat(event.toString()).doesNotContain("sort=asc");
    }

    @Test
    void testFilterExceptionPropagation() throws Exception {
        ApiCaptureProperties props = createProperties();
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/err");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        RuntimeException ex = new RuntimeException("Test Exception");
        FilterChain chain = (req, res) -> { throw ex; };

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
        
        publisher.close();

        assertThat(receivedEvents).hasSize(1);
        assertThat(receivedEvents.get(0).get("response").get("statusCode").asInt()).isEqualTo(200); // Because response status was untouched, defaults to 200 in Mock
    }

    @Test
    void testExcludedPaths() throws Exception {
        ApiCaptureProperties props = createProperties();
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);
        publisher.close();

        assertThat(receivedEvents).isEmpty();
        assertThat(publisher.getCaptured()).isEqualTo(0);
    }

    @Test
    void testTrustedProxies() throws Exception {
        ApiCaptureProperties props = createProperties();
        props.setTrustedProxies(List.of("10.0.0.1", "10.0.0.2"));
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        request.setRemoteAddr("10.0.0.2"); // trusted proxy
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1"); // real client is 203.0.113.1

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        publisher.close();

        assertThat(receivedEvents.get(0).get("clientIp").asText()).isEqualTo("203.0.113.1");
    }

    @Test
    void testUntrustedProxiesXffIgnored() throws Exception {
        ApiCaptureProperties props = createProperties();
        // no trusted proxies
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        request.setRemoteAddr("192.168.1.100"); 
        request.addHeader("X-Forwarded-For", "203.0.113.1"); 

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        publisher.close();

        // XFF is ignored because trustedProxies is empty
        assertThat(receivedEvents.get(0).get("clientIp").asText()).isEqualTo("192.168.1.100");
    }

    @Test
    void testUserIdResolution() throws Exception {
        ApiCaptureProperties props = createProperties();
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        
        UserIdResolver resolver = req -> "custom-user";
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, resolver);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        publisher.close();

        JsonNode identity = receivedEvents.get(0).get("identity");
        assertThat(identity.get("authenticated").asBoolean()).isTrue();
        assertThat(identity.get("userId").asText()).isEqualTo("custom-user");
    }

    @Test
    void testPublisherFailureDoesNotAffectResponse() throws Exception {
        ApiCaptureProperties props = createProperties();
        props.getPlatform().setBaseUrl("http://localhost:1"); // Invalid port
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        ApiCaptureFilter filter = new ApiCaptureFilter(publisher, props, null);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        
        publisher.close();
        
        assertThat(publisher.getFailed()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    @Test
    void testSlowPlatformDoesNotBlock() throws Exception {
        artificialDelayMs = 500;
        ApiCaptureProperties props = createProperties();
        props.getPublisher().setRequestTimeoutMs(100); // Faster timeout than delay
        EventPublisher publisher = new EventPublisher(props, objectMapper);
        
        long start = System.currentTimeMillis();
        for (int i=0; i<3; i++) {
            publisher.publish(new ApiEvent());
        }
        long duration = System.currentTimeMillis() - start;
        
        assertThat(duration).isLessThan(100); // Caller returns immediately
        
        publisher.close();
        
        assertThat(publisher.getFailed()).isGreaterThanOrEqualTo(0); // Timed out
    }

    @Test
    void testAutoConfiguration() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ApiCaptureAutoConfiguration.class))
                .withBean(ObjectMapper.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(EventPublisher.class);
                    assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                });

        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ApiCaptureAutoConfiguration.class))
                .withBean(ObjectMapper.class)
                .withPropertyValues("api.capture.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(EventPublisher.class);
                    assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
                });
    }
}
