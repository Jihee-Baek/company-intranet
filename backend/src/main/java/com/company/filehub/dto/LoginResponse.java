package com.company.filehub.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String employeeId;
    private String name;
    private String department;
    private Long userId;
    private String role;
}
