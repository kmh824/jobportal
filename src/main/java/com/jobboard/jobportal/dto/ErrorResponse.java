package com.jobboard.jobportal.dto;

import com.jobboard.jobportal.exception.ErrorCode;
import java.util.List;

/**
 * 공통 에러 응답 DTO
 */
public class ErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final ErrorCode errorCode;
    private final String traceId;
    private final String path;
    private final List<FieldError> errors;

    public ErrorResponse(String timestamp,
                         int status,
                         String error,
                         ErrorCode errorCode,
                         String traceId,
                         String path,
                         List<FieldError> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.traceId = traceId;
        this.path = path;
        this.errors = errors;
    }

    public String getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public ErrorCode getErrorCode() { return errorCode; }
    public String getTraceId() { return traceId; }
    public String getPath() { return path; }
    public List<FieldError> getErrors() { return errors; }

    /**
     * 필드별 에러 정보
     */
    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}