package com.jobboard.jobportal.service.impl;

import com.jobboard.jobportal.entity.RefreshToken;
import com.jobboard.jobportal.entity.User;
import com.jobboard.jobportal.repository.RefreshTokenRepository;
import com.jobboard.jobportal.security.JwtUtil;
import com.jobboard.jobportal.service.RefreshTokenService;
import com.jobboard.jobportal.util.HashUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtUtil jwtUtil;

    private static LocalDateTime toLdt(Date d) {
        return LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    @Override
    public void saveLoginToken(User user, String rawRefresh, String userAgent, String ip) {
        Jws<Claims> jws = jwtUtil.parse(rawRefresh);
        Claims c = jws.getBody();

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256Hex(rawRefresh))          // 원문 대신 해시 저장
                .issuedAt(toLdt(c.getIssuedAt()))
                .expiresAt(toLdt(c.getExpiration()))
                .userAgent(userAgent)
                .ip(ip)
                .build();
        repo.save(rt);
    }

    @Override
    public String rotate(String rawRefresh, User user, String userAgent, String ip) {
        // 1) 파싱/검증
        Jws<Claims> jws = jwtUtil.parse(rawRefresh);
        Claims c = jws.getBody();
        if (!"refresh".equals(c.get("type"))) throw new JwtException("not refresh");

        // 2) 저장소에서 활성 레코드 확인
        String oldHash = HashUtil.sha256Hex(rawRefresh);
        RefreshToken old = repo.findByTokenHash(oldHash)
                .orElseThrow(() -> new JwtException("unknown refresh"));
        if (!old.isActive()) throw new JwtException("revoked or expired");

        // 3) 새 refresh 발급/저장
        String newRefresh = jwtUtil.generateRefresh(user.getEmail());
        Jws<Claims> newJws = jwtUtil.parse(newRefresh);

        RefreshToken next = RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256Hex(newRefresh))
                .issuedAt(toLdt(newJws.getBody().getIssuedAt()))
                .expiresAt(toLdt(newJws.getBody().getExpiration()))
                .userAgent(userAgent)
                .ip(ip)
                .build();
        repo.save(next);

        // 4) 이전 토큰 무효화(회전)
        old.setRevokedAt(LocalDateTime.now());
        old.setReplacedBy(next);

        return newRefresh;
    }

    @Override
    public void revoke(String rawRefresh) {
        String h = HashUtil.sha256Hex(rawRefresh);
        repo.findByTokenHash(h).ifPresent(rt -> rt.setRevokedAt(LocalDateTime.now()));
    }
}
