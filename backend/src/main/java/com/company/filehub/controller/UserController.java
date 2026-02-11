package com.company.filehub.controller;

import com.company.filehub.dto.ApiResponse;
import com.company.filehub.dto.UserSearchResponse;
import com.company.filehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String keyword) {

        List<UserSearchResponse> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }
}
