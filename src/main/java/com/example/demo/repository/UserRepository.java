
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check duplicate email
    boolean existsByEmail(String email);

    // Check duplicate mobile number
    boolean existsByMobile(String mobile);

    // Find users by enum role: ADMIN, SALES, MANAGER, CUSTOMER
    List<User> findByRole(UserRole role);

    // Find users by Role entity name through roleEntity relationship
    List<User> findByRoleEntity_Name(String roleName);

    // Find users by Role entity ID
    List<User> findByRoleEntity_Id(Long roleId);
}