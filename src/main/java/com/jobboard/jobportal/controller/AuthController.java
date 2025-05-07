// src/main/java/com/jobboard/jobportal/controller/AuthController.java
package com.jobboard.jobportal.controller;

import com.jobboard.jobportal.dto.EmailRequest;
import com.jobboard.jobportal.dto.EmailVerifyRequest;
import com.jobboard.jobportal.dto.SignupRequest;
import com.jobboard.jobportal.dto.SignupResponse;
import com.jobboard.jobportal.service.AuthService;
import com.jobboard.jobportal.service.EmailVerificationService;
import com.jobboard.jobportal.service.TokenBlacklistService;
import com.jobboard.jobportal.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 인증 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailVerificationService emailService;
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtProperties jwtProperties;

    /**
     * 이메일 인증 코드 요청
     * HTTP 204 No Content 반환
     */
    @PostMapping("/email/request")
    public ResponseEntity<Void> requestEmail(@Valid @RequestBody EmailRequest req) {
        log.info("Email verification code requested for {}", req.getEmail());
        emailService.requestVerificationCode(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    /**
     * 이메일 인증 코드 검증
     * HTTP 204 No Content 반환
     */
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest req) {
        log.info("Email verification code submitted for {}", req.getEmail());
        emailService.verifyCode(req.getEmail(), req.getCode());
        return ResponseEntity.noContent().build();
    }

    /**
     * 회원가입 (이메일 인증 후)
     * HTTP 201 Created 반환, Location: /api/auth/me
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        log.info("Signup attempt for {}", req.getEmail());
        SignupResponse res = authService.register(req);
        log.info("Signup success for id={}, email={}", res.getId(), res.getEmail());
        return ResponseEntity
                .created(URI.create("/api/auth/me"))
                .body(res);
    }

    /**
     * 로그아웃 (리프레시 토큰 블랙리스트 등록)
     * HTTP 204 No Content 반환
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        log.info("Logout requested, blacklisting refresh token");
        if (refreshToken != null) {
            long ttl = jwtProperties.getRefreshExpiration();
            tokenBlacklistService.blacklist(refreshToken, ttl);
        }
        return ResponseEntity.noContent().build();
    }
}
