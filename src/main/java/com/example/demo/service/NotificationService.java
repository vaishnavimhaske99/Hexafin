package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.NotificationResponseDTO;

public interface NotificationService {

    List<NotificationResponseDTO> getMyNotifications(Long userId);

    List<NotificationResponseDTO> getUnreadNotifications(Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void createNotification(
            Long userId,
            String title,
            String message
    );
}