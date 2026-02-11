package com.company.filehub.dto;

import com.company.filehub.entity.User;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSearchResponse {
    private Long id;
    private String employeeId;
    private String name;
    private String department;

    public static UserSearchResponse from(User user) {
        return UserSearchResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .department(user.getDepartmentName())
                .build();
    }
}
