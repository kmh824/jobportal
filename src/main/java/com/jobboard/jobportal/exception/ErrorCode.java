package com.jobboard.jobportal.exception;

/**
 * 애플리케이션별 에러 코드 정의
 */
public enum ErrorCode {
    VALIDATION_ERROR,
    AUTH_INVALID_CODE,
    AUTH_EXPIRED_CODE,
    AUTH_BLOCKED,
    INTERNAL_SERVER_ERROR
}