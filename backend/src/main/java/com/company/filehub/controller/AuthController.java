package com.company.filehub.controller;

import com.company.filehub.dto.*;
import com.company.filehub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for employeeId: {}, name: {}", request.getEmployeeId(), request.getName());
        try {
            authService.signup(request);
            log.info("Signup completed successfully for employeeId: {}", request.getEmployeeId());
            return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다", null));
        } catch (Exception e) {
            log.error("Signup failed for employeeId: {}", request.getEmployeeId(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for employeeId: {}", request.getEmployeeId());
        try {
            LoginResponse response = authService.login(request);
            log.info("Login successful for employeeId: {}", request.getEmployeeId());
            return ResponseEntity.ok(ApiResponse.ok("로그인 성공", response));
        } catch (Exception e) {
            log.error("Login failed for employeeId: {}", request.getEmployeeId(), e);
            throw e;
        }
    }
}
