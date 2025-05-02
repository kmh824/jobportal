// src/main/java/com/jobboard/jobportal/controller/AuthController.java
package com.jobboard.jobportal.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;             // ← 추가!
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobboard.jobportal.service.TokenBlacklistService;
import com.jobboard.jobportal.config.JwtProperties;

@RestController
public class AuthController {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    public AuthController(TokenBlacklistService tokenBlacklistService,
                          JwtProperties jwtProperties) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            long ttl = jwtProperties.getRefreshExpiration();
            tokenBlacklistService.blacklist(refreshToken, ttl);

            Cookie expired = new Cookie("refreshToken", null);
            expired.setHttpOnly(true);
            expired.setSecure(true);
            expired.setPath("/");
            expired.setMaxAge(0);
            response.addCookie(expired);
        }
        return ResponseEntity.ok().build();
    }
}
