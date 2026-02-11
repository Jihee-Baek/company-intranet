package com.company.filehub.service;

import com.company.filehub.dto.UserSearchResponse;
import com.company.filehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String keyword) {
        return userRepository
                .findByNameContainingOrEmployeeIdContainingOrDepartment_NameContaining(keyword, keyword, keyword)
                .stream()
                .map(UserSearchResponse::from)
                .toList();
    }
}
