
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Announcement;

@Repository
public interface AnnouncementRepository
        extends JpaRepository<Announcement, Long> {

    // Get all announcements, latest first
    List<Announcement> findAllByOrderByCreatedAtDesc();

    // Get announcements created by a particular Admin
    List<Announcement> findByCreatedBy_IdOrderByCreatedAtDesc(Long adminId);
}
