package com.example.seoulproject.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginDto {
    @NotBlank(message = "사용하실 이메일을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,30}$", message = "이메일 형식이 올바르지 않습니다.")
    @Size(min = 1, max = 30, message = "이메일은 최소 1자에서 최대 30자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "영문과 숫자,특수기호를 조합하여 8~20글자 미만으로 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,20}$", message = "비밀번호는 영문과 숫자,특수기호를 조합하여 8~20글자 미만으로 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 최소 8자에서 최대 20자까지 입력 가능합니다.")
    private String password;
}
