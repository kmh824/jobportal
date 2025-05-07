// src/main/java/com/jobboard/jobportal/config/VerificationProperties.java
package com.jobboard.jobportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.verification")
public class VerificationProperties {
    private int codeLength;
    private long expireMinutes;
    private long blockHours;
    private long maxAttempts;
}
