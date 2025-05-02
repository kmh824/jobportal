// src/main/java/com/jobboard/jobportal/service/TokenBlacklistService.java
package com.jobboard.jobportal.service;

public interface TokenBlacklistService {
    void blacklist(String token, long ttlMillis);
    boolean isBlacklisted(String token);
}
