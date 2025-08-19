package com.jobboard.jobportal.controller;

import com.jobboard.jobportal.repository.UserRepository;
import com.jobboard.jobportal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // me 응답 시 id 추출용(선택)

    // ===== DTO =====
    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {}

    public record UserSummary(Long id, String email, List<String> roles) {}

    // ===== 로그인 =====
    @PostMapping("/login")
    public ResponseEntity<UserSummary> login(@Valid @RequestBody LoginRequest req,
                                             HttpServletResponse res) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserDetails principal = (UserDetails) auth.getPrincipal();

        // roles from authorities → ["USER","ADMIN", ...]
        List<String> roles = principal.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();

        String access  = jwtUtil.generateAccess(principal.getUsername(), roles);
        String refresh = jwtUtil.generateRefresh(principal.getUsername());

        // Access → 헤더
        res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + access);

        // Refresh → HttpOnly 쿠키 (로컬 정책: SameSite=Lax, Secure=false)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(false)          // stage/prod 에서는 true + Domain=.jobportal.site (예시)
                .sameSite("Lax")        // stage/prod 에서는 "None"
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // id는 선택: 이메일로 조회해서 넣어줌
        Long id = userRepository.findByEmail(principal.getUsername())
                .map(u -> u.getId())
                .orElse(null);

        return ResponseEntity.ok(new UserSummary(id, principal.getUsername(), roles));
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
}
