// src/main/java/com/jobboard/jobportal/service/impl/AuthServiceImpl.java
package com.jobboard.jobportal.service.impl;

import com.jobboard.jobportal.dto.SignupRequest;
import com.jobboard.jobportal.dto.SignupResponse;
import com.jobboard.jobportal.entity.User;
import com.jobboard.jobportal.exception.EmailAlreadyExistsException;
import com.jobboard.jobportal.repository.UserRepository;
import com.jobboard.jobportal.service.AuthService;
import com.jobboard.jobportal.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService 구현체
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService verificationService;

    @Override
    @Transactional
    public SignupResponse register(SignupRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        log.info("회원가입 시도: email={} code=****", email);

        // 1) 이메일 인증 코드 검증
        verificationService.verifyCode(email, code);

        // 2) 중복 이메일 검사
        if (userRepository.existsByEmail(email)) {
            log.error("중복 이메일 가입 시도: {}", email);
            throw new EmailAlreadyExistsException("이미 사용 중인 이메일입니다: " + email);
        }

        // 3) 비밀번호 해시
        String encodedPw = passwordEncoder.encode(request.getPassword());

        // 4) User 엔티티 생성 및 저장
        User user = User.builder()
                .email(email)
                .password(encodedPw)
                .role(User.Role.ROLE_USER)
                .build();
        User saved = userRepository.save(user);

        log.info("회원가입 성공: id={} email={}", saved.getId(), email);

        // 5) 응답 DTO 반환
        return new SignupResponse(saved.getId(), saved.getEmail(), saved.getRole().name());
    }
}