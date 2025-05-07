// src/main/java/com/jobboard/jobportal/exception/InvalidVerificationCodeException.java
package com.jobboard.jobportal.exception;

public class InvalidVerificationCodeException extends RuntimeException {
    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}
