package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
    
    List<User> findByRole(UserRole role);
    List<User> findByRoleEntity_Name(String roleName);
    List<User> findByRoleEntity_Id(Long roleId);

    long countByRole(UserRole role);

    // ✅ hierarchy support
    List<User> findByReportsTo_Id(Long reportsToId);

    // ✅ Eager load reportsTo to avoid N+1
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.reportsTo")
    List<User> findAllWithReportsTo();
}