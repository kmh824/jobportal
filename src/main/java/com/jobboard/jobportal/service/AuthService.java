// src/main/java/com/jobboard/jobportal/service/AuthService.java
package com.jobboard.jobportal.service;

import com.jobboard.jobportal.dto.SignupRequest;
import com.jobboard.jobportal.dto.SignupResponse;

/**
 * 인증(회원가입/로그인) 관련 서비스 인터페이스
 */
public interface AuthService {
    /**
     * 회원가입 처리
     * @param request 이메일, 인증 코드, 비밀번호 정보
     * @return 가입 성공 결과 DTO
     */
    SignupResponse register(SignupRequest request);
}