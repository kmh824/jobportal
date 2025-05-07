package com.jobboard.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 회원가입 응답 DTO
 */
@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long id;
    private String email;
    private String role;
}