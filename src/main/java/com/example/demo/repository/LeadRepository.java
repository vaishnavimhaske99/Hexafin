package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Lead;
import com.example.demo.entity.LeadStatus;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<Lead> findByAssignedToId(Long userId);

    // Dashboard counts

    long countByStatus(LeadStatus status);

    long countByAssignedToIsNotNull();

    long countByAssignedToIsNull();

    long countByAssignedToId(Long userId);

    long countByAssignedToIdAndStatus(Long userId, LeadStatus status);
}