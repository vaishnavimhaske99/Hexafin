package com.example.demo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.UserSupervisorHistory;

public interface UserSupervisorHistoryRepository extends JpaRepository<UserSupervisorHistory, Long> {
    // चालू असलेला Supervisor शोधण्यासाठी
    Optional<UserSupervisorHistory> findByUserIdAndEndedAtIsNull(Long userId);
}