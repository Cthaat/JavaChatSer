package com.example.javachat.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expiration) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = "change-this-secret-before-production";
        }
        if (expiration <= 0) {
            expiration = 86_400_000L;
        }
    }
}
