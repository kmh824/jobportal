// src/main/java/com/jobboard/jobportal/exception/VerificationExpiredException.java
package com.jobboard.jobportal.exception;

public class VerificationExpiredException extends RuntimeException {
    public VerificationExpiredException(String message) {
        super(message);
    }
}
