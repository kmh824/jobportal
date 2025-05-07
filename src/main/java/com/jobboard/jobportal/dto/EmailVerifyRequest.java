// src/main/java/com/jobboard/jobportal/dto/EmailVerifyRequest.java
package com.jobboard.jobportal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyRequest {
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 주소를 입력해주세요")
    private String email;

    @NotBlank(message = "인증 코드는 필수입니다")
    @Pattern(regexp = "\\d{6}", message = "6자리 숫자 코드를 입력하세요")
    private String code;

}
