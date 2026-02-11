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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("이미 등록된 사번입니다: " + request.getEmployeeId());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다"));
        }

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .department(department)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사번입니다"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmployeeId());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .department(user.getDepartmentName())
                .build();
    }
}
