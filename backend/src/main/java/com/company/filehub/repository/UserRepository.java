package com.company.filehub.repository;

import com.company.filehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    List<User> findByNameContainingOrEmployeeIdContainingOrDepartment_NameContaining(
            String name, String employeeId, String department);
}
