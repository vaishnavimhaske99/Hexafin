
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationCategory;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // All active notifications for a user
    Page<Notification> findByUser_IdAndIsArchivedFalseOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Unread notifications
    Page<Notification> findByUser_IdAndIsReadFalseAndIsArchivedFalseOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Unread notification count
    long countByUser_IdAndIsReadFalseAndIsArchivedFalse(Long userId);

    // Total notification count
    long countByUser_IdAndIsArchivedFalse(Long userId);

    // Find notification belonging to a specific user
    Optional<Notification> findByIdAndUser_Id(
            Long notificationId,
            Long userId
    );

    // Notifications by category
    Page<Notification> findByUser_IdAndCategoryAndIsArchivedFalseOrderByCreatedAtDesc(
            Long userId,
            NotificationCategory category,
            Pageable pageable
    );

    // Archived notifications
    Page<Notification> findByUser_IdAndIsArchivedTrueOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Mark all unread notifications as read
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = CURRENT_TIMESTAMP
        WHERE n.user.id = :userId
          AND n.isRead = false
          AND n.isArchived = false
    """)
    int markAllAsRead(@Param("userId") Long userId);

    // Archive a notification belonging to a specific user
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Notification n
        SET n.isArchived = true
        WHERE n.id = :notificationId
          AND n.user.id = :userId
    """)
    int archiveNotification(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );

    // Delete expired notifications (for scheduled cleanup)
    @Modifying
    @Query("""
        DELETE FROM Notification n
        WHERE n.expiresAt IS NOT NULL
          AND n.expiresAt < CURRENT_TIMESTAMP
    """)
    int deleteExpiredNotifications();
}
