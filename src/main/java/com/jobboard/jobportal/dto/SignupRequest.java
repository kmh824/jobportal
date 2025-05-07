package com.jobboard.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank(message = "이메일 인증이 필요합니다")
    private String email;

    @NotBlank(message = "인증 코드는 필수입니다")
    @Pattern(regexp = "\\d{6}", message = "6자리 숫자 코드를 입력하세요")
    private String code;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
            // 길이 8~64, 대문자 1, 소문자 1, 숫자 1, 특수문자 1 이상
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,64}$",
            message = "비밀번호는 8자 이상 64자 이하이고, 대문자·소문자·숫자·특수문자를 각각 최소 1자 포함해야 합니다"
    )
    private String password;
}
