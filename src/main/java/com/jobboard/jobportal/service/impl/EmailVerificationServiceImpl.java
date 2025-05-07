// src/main/java/com/jobboard/jobportal/service/impl/EmailVerificationServiceImpl.java
package com.jobboard.jobportal.service.impl;

import com.jobboard.jobportal.config.VerificationProperties;
import com.jobboard.jobportal.entity.EmailVerification;
import com.jobboard.jobportal.exception.InvalidVerificationCodeException;
import com.jobboard.jobportal.exception.VerificationBlockedException;
import com.jobboard.jobportal.exception.VerificationExpiredException;
import com.jobboard.jobportal.repository.EmailVerificationRepository;
import com.jobboard.jobportal.service.EmailVerificationService;
import com.jobboard.jobportal.service.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationRepository repository;
    private final EmailSender emailSender;
    private final VerificationProperties props;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public void requestVerificationCode(String email) {
        int codeLength = props.getCodeLength();
        long expireMinutes = props.getExpireMinutes();
        long blockHours = props.getBlockHours();

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // 기존 레코드 검사 및 삭제
        repository.findByEmail(email).ifPresent(ev -> {
            if (ev.getBlockedUntil() != null && ev.getBlockedUntil().isAfter(now)) {
                throw new VerificationBlockedException(
                        String.format("인증 시도가 차단되었습니다. %d시간 후 다시 시도하세요.", blockHours)
                );
            }
            repository.deleteByEmail(email);
        });

        // 코드 생성 및 저장
        String code = String.format("%0" + codeLength + "d", RANDOM.nextInt((int) Math.pow(10, codeLength)));
        LocalDateTime expiresAt = now.plusMinutes(expireMinutes);
        EmailVerification ev = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiresAt(expiresAt)
                .attemptCount(0)
                .blockedUntil(null)
                .build();
        repository.save(ev);

        // 이메일 전송
        String subject = "[JobPortal] 이메일 인증 코드 안내";
        String text = String.format("인증 코드는 %s 입니다. %d분 내에 입력해주세요.", code, expireMinutes);
        emailSender.send(email, subject, text);
    }

    @Override
    @Transactional
    public void verifyCode(String email, String code) {
        long blockHours = props.getBlockHours();
        long maxAttempts = props.getMaxAttempts();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        EmailVerification ev = repository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidVerificationCodeException("인증 요청을 먼저 진행해주세요.")
                );

        // 차단 상태 검사
        if (ev.getBlockedUntil() != null && ev.getBlockedUntil().isAfter(now)) {
            throw new VerificationBlockedException(
                    String.format("인증 시도가 차단되었습니다. %d시간 후 다시 시도하세요.", blockHours)
            );
        }

        // 만료 검사
        if (ev.getExpiresAt().isBefore(now)) {
            repository.deleteByEmail(email);
            throw new VerificationExpiredException("인증 코드가 만료되었습니다.");
        }

        // 코드 불일치 검사 및 시도 횟수 관리
        if (!ev.getCode().equals(code)) {
            ev.setAttemptCount(ev.getAttemptCount() + 1);
            if (ev.getAttemptCount() >= maxAttempts) {
                ev.setBlockedUntil(now.plusHours(blockHours));
            }
            repository.save(ev);
            if (ev.getAttemptCount() >= maxAttempts) {
                throw new VerificationBlockedException(
                        String.format("인증 시도가 차단되었습니다. %d시간 후 다시 시도하세요.", blockHours)
                );
            }
            throw new InvalidVerificationCodeException("인증 코드가 올바르지 않습니다.");
        }

        // 성공 시 레코드 삭제
        repository.deleteByEmail(email);
    }
}
