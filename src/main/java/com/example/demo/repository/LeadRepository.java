package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    // ✅ underscore सह
    List<Lead> findByAssignedTo_Id(Long assignedToId);

    // Dashboard counts
    long countByStatus(LeadStatus status);

    long countByAssignedToIsNotNull();

    long countByAssignedToIsNull();

    // ✅ underscore सह
    long countByAssignedTo_Id(Long userId);

    // ✅ underscore सह
    long countByAssignedTo_IdAndStatus(Long userId, LeadStatus status);
}