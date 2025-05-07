package com.jobboard.jobportal.exception;

import com.jobboard.jobportal.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        BindingResult br = ex.getBindingResult();
        List<ErrorResponse.FieldError> fieldErrors = br.getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse body = buildErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCode(InvalidVerificationCodeException ex,
                                                           HttpServletRequest request) {
        return respond(request, HttpStatus.BAD_REQUEST, ErrorCode.AUTH_INVALID_CODE,
                Collections.singletonList(new ErrorResponse.FieldError("global", ex.getMessage())));
    }

    @ExceptionHandler(VerificationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(VerificationExpiredException ex,
                                                       HttpServletRequest request) {
        return respond(request, HttpStatus.GONE, ErrorCode.AUTH_EXPIRED_CODE,
                Collections.singletonList(new ErrorResponse.FieldError("global", ex.getMessage())));
    }

    @ExceptionHandler(VerificationBlockedException.class)
    public ResponseEntity<ErrorResponse> handleBlocked(VerificationBlockedException ex,
                                                       HttpServletRequest request) {
        return respond(request, HttpStatus.TOO_MANY_REQUESTS, ErrorCode.AUTH_BLOCKED,
                Collections.singletonList(new ErrorResponse.FieldError("global", ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex,
                                                   HttpServletRequest request) {
        return respond(request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR,
                Collections.singletonList(new ErrorResponse.FieldError("global", "서버 오류가 발생했습니다")));
    }

    private ResponseEntity<ErrorResponse> respond(HttpServletRequest request,
                                                  HttpStatus status,
                                                  ErrorCode code,
                                                  List<ErrorResponse.FieldError> errors) {
        ErrorResponse body = buildErrorResponse(request.getRequestURI(), status, code, errors);
        return ResponseEntity.status(status).body(body);
    }

    private ErrorResponse buildErrorResponse(String path,
                                             HttpStatus status,
                                             ErrorCode code,
                                             List<ErrorResponse.FieldError> errors) {
        String timestamp = LocalDateTime.now().format(FMT);
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        return new ErrorResponse(
                timestamp,
                status.value(),
                status.getReasonPhrase(),
                code,
                traceId,
                path,
                errors
        );
    }
}
