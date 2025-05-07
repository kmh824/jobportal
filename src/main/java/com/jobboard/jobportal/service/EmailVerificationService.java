package com.jobboard.jobportal.service;

/**
 * 이메일 인증 코드 발송 및 검증 서비스
 */
public interface EmailVerificationService {
    /**
     * 이메일로 인증 코드를 생성하여 전송합니다.
     * @param email 인증 코드를 보낼 이메일 주소
     * @throws com.jobboard.jobportal.exception.VerificationBlockedException
     *         차단된 경우
     */
    void requestVerificationCode(String email);

    /**
     * 사용자가 입력한 코드로 이메일 인증을 검증합니다.
     * @param email 인증 대상 이메일
     * @param code 사용자 입력 코드
     * @throws com.jobboard.jobportal.exception.VerificationExpiredException
     *         만료된 코드인 경우
     * @throws com.jobboard.jobportal.exception.InvalidVerificationCodeException
     *         코드가 불일치하는 경우
     * @throws com.jobboard.jobportal.exception.VerificationBlockedException
     *         인증 시도가 차단된 경우
     */
    void verifyCode(String email, String code);
}

