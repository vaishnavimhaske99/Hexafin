
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.UserLocationHistory;

public interface UserLocationHistoryRepository
        extends JpaRepository<UserLocationHistory, Long> {

    List<UserLocationHistory> findByUserIdOrderByRecordedAtDesc(Long userId);
}