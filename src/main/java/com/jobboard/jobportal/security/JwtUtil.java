// com.jobboard.jobportal.security.JwtUtil
package com.jobboard.jobportal.security;

import com.jobboard.jobportal.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccess(String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(email)
                .addClaims(Map.of("roles", roles, "type", "access"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getAccessExpiration())))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefresh(String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(email)
                .addClaims(Map.of("type", "refresh"))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getRefreshExpiration())))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(props.getIssuer())
                .setAllowedClockSkewSeconds(120) // 2분 스큐 허용
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token);
    }
}
