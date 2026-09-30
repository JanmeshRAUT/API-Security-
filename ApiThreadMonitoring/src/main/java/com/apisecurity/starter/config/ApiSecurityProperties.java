package com.apisecurity.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
@ConfigurationProperties(prefix = "api.security")
public class ApiSecurityProperties {

    /**
     * Whether the API security framework is enabled.
     */
    private boolean enabled = true;

    /**
     * The unique identifier for this application.
     */
    private String applicationId = "default-application";

    private final Platform platform = new Platform();
    private final Monitoring monitoring = new Monitoring();
    private final Behavior behavior = new Behavior();
    private final Detection detection = new Detection();

    @Data
    public static class Platform {
        private String baseUrl = "http://localhost:8085";
        private String apiKey = "";
    }

    @Data
    public static class Monitoring {
        private boolean captureRequestBody = false;
        private boolean captureResponseBody = false;
        private boolean captureHeaders = false;
        private List<String> excludedPaths = List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico"
        );
        private List<String> trustedProxies = new ArrayList<>();
    }

    @Data
    public static class Behavior {
        private Mode mode = Mode.MONITOR;
        private int windowDurationSeconds = 300;
        private int maxTrackedUsers = 1000;
        private int maxTrackedIps = 1000;

        public enum Mode {
            MONITOR, ALERT, BLOCK
        }
    }
    
    @Data
    public static class Detection {
        private final Authentication authentication = new Authentication();
        private final ObjectAccess objectAccess = new ObjectAccess();
        private final Privacy privacy = new Privacy();
        
        @Data
        public static class Authentication {
            private List<String> endpointPatterns = List.of("/api/auth/login", "/api/login", "/auth/login");
        }
        
        @Data
        public static class ObjectAccess {
            private boolean enabled = true;
            private List<String> idParameterNames = List.of("id", "userId", "orderId", "productId", "accountId", "objectId");
            private int maxTrackedObjectIds = 1000;
        }
        
        @Data
        public static class Privacy {
            private boolean hashIdentifiers = true;
        }
    }
}
