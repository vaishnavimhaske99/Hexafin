
package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationRequestDTO;
import com.example.demo.entity.NotificationResponseDTO;


public interface NotificationService {

    // Create notification for a recipient
    NotificationResponseDTO createNotification(
            Long recipientId,
            Long actorId,
            NotificationRequestDTO request
    );

    // Get user's notifications
    Page<NotificationResponseDTO> getNotifications(
            Long userId,
            Pageable pageable
    );

    // Get unread notifications
    Page<NotificationResponseDTO> getUnreadNotifications(
            Long userId,
            Pageable pageable
    );

    // Get unread notification count
    long getUnreadCount(Long userId);

    // Get notifications by category
    Page<NotificationResponseDTO> getNotificationsByCategory(
            Long userId,
            NotificationCategory category,
            Pageable pageable
    );

    // Mark one notification as read
    NotificationResponseDTO markAsRead(
            Long notificationId,
            Long userId
    );

    // Mark all notifications as read
    int markAllAsRead(Long userId);

    // Archive notification
    void archiveNotification(
            Long notificationId,
            Long userId
    );

    // Get archived notifications
    Page<NotificationResponseDTO> getArchivedNotifications(
            Long userId,
            Pageable pageable
    );
}
