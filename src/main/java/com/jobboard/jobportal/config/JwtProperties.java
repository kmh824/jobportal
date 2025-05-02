// src/main/java/com/jobboard/jobportal/config/JwtProperties.java
package com.jobboard.jobportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JwtProperties {
    private String secret;
    private long accessExpiration;
    private long refreshExpiration;
    // getters & setters
}

