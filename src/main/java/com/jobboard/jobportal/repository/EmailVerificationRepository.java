// src/main/java/com/jobboard/jobportal/repository/EmailVerificationRepository.java
package com.jobboard.jobportal.repository;

import com.jobboard.jobportal.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

    void deleteByEmail(String email);

    // 만료된 레코드 일괄 삭제용 메서드
    @Modifying
    @Transactional
    long deleteByExpiresAtBefore(LocalDateTime now);

    // 검증 로직 편의를 위한 메서드
    Optional<EmailVerification> findByEmailAndCodeAndExpiresAtAfter(
            String email, String code, LocalDateTime now);
}
