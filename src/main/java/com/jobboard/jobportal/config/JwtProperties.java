// com.jobboard.jobportal.config.JwtProperties
package com.jobboard.jobportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private String secret;
    private long accessExpiration;   // seconds
    private long refreshExpiration;  // seconds
}
