package com.company.filehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {

    @NotBlank(message = "사번은 필수입니다")
    @Size(max = 20)
    private String employeeId;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50)
    private String name;

    private Long departmentId;
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 4, max = 100)
    private String password;
}
