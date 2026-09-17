package com.eventmgmt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds app.jwt.* properties from application.properties into a typed bean.
 * JwtTokenProvider currently reads these directly via @Value, but this
 * bean is kept available for any component that prefers typed injection.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {

    private String secret;
    private long expirationMs;
}