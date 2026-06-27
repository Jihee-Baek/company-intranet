package com.company.filehub.service;

import com.company.filehub.dto.LoginRequest;
import com.company.filehub.dto.LoginResponse;
import com.company.filehub.dto.SignupRequest;
import com.company.filehub.entity.Department;
import com.company.filehub.entity.User;
import com.company.filehub.repository.DepartmentRepository;
import com.company.filehub.repository.UserRepository;
import com.company.filehub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
        log.info("Processing signup for employeeId: {}", request.getEmployeeId());
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            log.warn("Signup failed: EmployeeId {} already exists", request.getEmployeeId());
            throw new IllegalArgumentException("이미 등록된 사번입니다: " + request.getEmployeeId());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> {
                        log.warn("Signup failed: DepartmentId {} not found", request.getDepartmentId());
                        return new IllegalArgumentException("존재하지 않는 부서입니다");
                    });
        }

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .department(department)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("Successfully registered user ID: {} with employeeId: {}", user.getId(), user.getEmployeeId());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Processing login for employeeId: {}", request.getEmployeeId());
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> {
                    log.warn("Login failed: EmployeeId {} not found", request.getEmployeeId());
                    return new IllegalArgumentException("존재하지 않는 사번입니다");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Incorrect password for employeeId: {}", request.getEmployeeId());
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmployeeId());
        log.info("Authentication successful for user ID: {} (employeeId: {})", user.getId(), user.getEmployeeId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .department(user.getDepartmentName())
                .role(user.getRole().name())
                .build();
    }
}
