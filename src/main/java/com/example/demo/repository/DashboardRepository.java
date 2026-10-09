package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Lead;

public interface DashboardRepository extends JpaRepository<Lead, Long> {

    long countByStatus(com.example.demo.entity.LeadStatus status);

    long countByAssignedToIsNotNull();

    long countByAssignedToIsNull();
}