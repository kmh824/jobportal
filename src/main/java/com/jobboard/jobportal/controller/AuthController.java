package com.jobboard.jobportal.controller;

import com.jobboard.jobportal.repository.UserRepository;
import com.jobboard.jobportal.security.JwtUtil;
import com.jobboard.jobportal.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // me 응답 시 id 추출용(선택)
    private final RefreshTokenService refreshTokenService; // ← 인터페이스 주입

    // ===== DTO =====
    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}

    public record UserSummary(Long id, String email, List<String> roles) {}

    // ===== 로그인 =====
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req,
                                   HttpServletRequest request,        // ← 이름: request
                                   HttpServletResponse response) {    // ← 이름: response
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );

            UserDetails principal = (UserDetails) auth.getPrincipal();
            var roles = principal.getAuthorities().stream()
                    .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                    .toList();

            String access  = jwtUtil.generateAccess(principal.getUsername(), roles);
            String refresh = jwtUtil.generateRefresh(principal.getUsername());

            // 헤더/쿠키
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + access);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                    .httpOnly(true).secure(false).sameSite("Lax").path("/")
                    .maxAge(Duration.ofDays(7)).build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 🔹 DB 저장 (UA/IP 포함)
            var user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
            refreshTokenService.saveLoginToken(
                    user,
                    refresh,
                    request.getHeader("User-Agent"),   // ← getHeader 사용
                    clientIp(request)                  // ← 아래 헬퍼 메서드
            );

            return ResponseEntity.ok(Map.of(
                    "email", principal.getUsername(),
                    "roles", roles
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "code", "AUTH_INVALID_CREDENTIALS",
                    "message", "아이디 또는 비밀번호가 일치하지 않습니다."
            ));
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String fwd = req.getHeader("X-Forwarded-For");
        return (fwd != null && !fwd.isBlank()) ? fwd.split(",")[0].trim() : req.getRemoteAddr();
    }



    // ===== 내 정보(보호) =====
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();
        return ResponseEntity.ok(Map.of(
                "email", auth.getName(),
                "roles", roles
        ));
    }

    // ===== 로그아웃(Refresh 쿠키 삭제) =====
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {
        ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)     // stage/prod: true + Domain 동일
                .sameSite("Lax")   // stage/prod: "None"
                .path("/")
                .maxAge(0)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String refresh,
            HttpServletResponse res
    ) {
        // 1) 쿠키 없는 경우
        if (!StringUtils.hasText(refresh)) {
            return ResponseEntity.status(401).build();
        }

        try {
            // 2) Refresh 파싱 및 검증
            Jws<Claims> jws = jwtUtil.parse(refresh);
            Claims c = jws.getBody();
            if (!"refresh".equals(c.get("type"))) {
                return ResponseEntity.status(401).build();
            }

            // 3) 주체(email) 기준 권한 조회(간단 버전)
            String email = c.getSubject();
            var roles = userRepository.findByEmail(email)
                    .map(u -> java.util.List.of(u.getRole().name().replaceFirst("^ROLE_", "")))
                    .orElse(java.util.List.of("USER"));

            // 4) 새 Access 발급 → Authorization 헤더로 반환
            String access = jwtUtil.generateAccess(email, roles);
            res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + access);

            return ResponseEntity.noContent().build();
        } catch (JwtException e) {
            // (선택) 잘못된 쿠키는 즉시 삭제해 UX/보안 개선
            ResponseCookie delete = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)   // stage/prod에서는 true + Domain/SameSite=None
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(0)
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, delete.toString());
            return ResponseEntity.status(401).build();
        }
    }

}
