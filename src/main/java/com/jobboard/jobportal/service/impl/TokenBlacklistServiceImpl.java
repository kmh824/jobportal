// src/main/java/com/jobboard/jobportal/service/impl/TokenBlacklistServiceImpl.java
package com.jobboard.jobportal.service.impl;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.jobboard.jobportal.service.TokenBlacklistService;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final StringRedisTemplate redis;

    public TokenBlacklistServiceImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void blacklist(String token, long ttlMillis) {
        redis.opsForValue().set("blacklist:" + token, "", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey("blacklist:" + token));
    }
}
