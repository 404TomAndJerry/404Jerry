package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.") // 이메일 형식 검증
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  private String password;

  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "닉네임은 특수문자 제외 2~10자리여야 합니다.") // 닉네임 형식 검증
  private String nickname;
  private Integer age;
}