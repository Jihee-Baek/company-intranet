package com.company.filehub.controller;

import com.company.filehub.dto.ApiResponse;
import com.company.filehub.dto.UserSearchResponse;
import com.company.filehub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String keyword) {
        log.info("Request received: GET /api/users/search (keyword: {})", keyword);
        try {
            List<UserSearchResponse> users = userService.searchUsers(keyword);
            log.info("Successfully found {} users matching keyword: {}", users.size(), keyword);
            return ResponseEntity.ok(ApiResponse.ok(users));
        } catch (Exception e) {
            log.error("Failed to search users with keyword: {}", keyword, e);
            throw e;
        }
    }
}
