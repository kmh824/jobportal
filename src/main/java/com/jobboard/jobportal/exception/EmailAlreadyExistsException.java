// src/main/java/com/jobboard/jobportal/exception/EmailAlreadyExistsException.java
package com.jobboard.jobportal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 이미 존재하는 이메일로 회원가입 시도할 때 발생하는 예외
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
