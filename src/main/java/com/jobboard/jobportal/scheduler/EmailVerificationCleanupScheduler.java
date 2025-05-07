package com.jobboard.jobportal.scheduler;

import com.jobboard.jobportal.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 만료된 이메일 인증 레코드를 주기적으로 삭제하는 스케줄러
 */
@Component
public class EmailVerificationCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationCleanupScheduler.class);
    private final EmailVerificationRepository repository;

    public EmailVerificationCleanupScheduler(EmailVerificationRepository repository) {
        this.repository = repository;
    }

    /**
     * 매시 정각에 만료된 인증 코드 삭제
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredVerifications() {
        LocalDateTime now = LocalDateTime.now();
        long deleted = repository.deleteByExpiresAtBefore(now);
        log.info("Expired EmailVerification entries deleted: {} at {}", deleted, now);
    }
}