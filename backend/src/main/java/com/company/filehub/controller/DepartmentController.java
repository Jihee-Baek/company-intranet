package com.company.filehub.controller;

import com.company.filehub.entity.Department;
import com.company.filehub.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        log.info("Request received: GET /api/departments");
        try {
            List<Department> departments = departmentRepository.findAll();
            log.info("Successfully fetched {} departments", departments.size());
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            log.error("Failed to fetch departments from database", e);
            throw e;
        }
    }
}
