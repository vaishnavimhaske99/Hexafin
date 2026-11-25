
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.AnnouncementRecipient;

@Repository
public interface AnnouncementRecipientRepository
        extends JpaRepository<AnnouncementRecipient, Long> {

    // Get all announcements received by a user
    List<AnnouncementRecipient> findByUser_IdOrderByAnnouncement_CreatedAtDesc(
            Long userId);

    // Get unread announcements of a user
    List<AnnouncementRecipient> findByUser_IdAndIsReadFalseOrderByAnnouncement_CreatedAtDesc(
            Long userId);

    // Count unread announcements
    long countByUser_IdAndIsReadFalse(Long userId);

    // Find a particular user's recipient record
    Optional<AnnouncementRecipient> findByAnnouncement_IdAndUser_Id(
            Long announcementId, Long userId);

    // Get all recipients of an announcement
    List<AnnouncementRecipient> findByAnnouncement_Id(Long announcementId);
}
