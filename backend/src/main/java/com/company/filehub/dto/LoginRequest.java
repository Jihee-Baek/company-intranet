package com.company.filehub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank(message = "사번은 필수입니다")
    private String employeeId;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
