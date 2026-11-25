
package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.LeadActivity;

public interface LeadActivityRepository
        extends JpaRepository<LeadActivity, Long> {

    List<LeadActivity> findByLeadIdOrderByActivityDateTimeDesc(
            Long leadId
    );

    // Today's activities of all users
    List<LeadActivity> findByActivityDateTimeBetweenOrderByActivityDateTimeDesc(
            LocalDateTime start,
            LocalDateTime end
    );
}
