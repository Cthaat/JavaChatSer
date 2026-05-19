package com.example.javachat.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        Boolean allowCredentials,
        Long maxAge
) {

    public CorsProperties {
        allowedOrigins = defaultIfBlank(allowedOrigins, List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        allowedMethods = defaultIfBlank(allowedMethods, List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        allowedHeaders = defaultIfBlank(allowedHeaders, List.of("Authorization", "Content-Type"));
        allowCredentials = allowCredentials == null || allowCredentials;
        maxAge = maxAge == null ? 3600L : maxAge;
    }

    private static List<String> defaultIfBlank(List<String> value, List<String> defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }
}
