package com.company.filehub.security;

import com.company.filehub.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal {
    private final User user;

    public Long getId() {
        return user.getId();
    }

    public String getEmployeeId() {
        return user.getEmployeeId();
    }

    public String getName() {
        return user.getName();
    }
}
