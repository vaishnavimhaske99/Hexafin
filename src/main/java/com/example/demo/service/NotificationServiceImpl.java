package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.NotificationResponseDTO;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // CREATE NOTIFICATION
    // =========================

    @Override
    @Transactional
    public void createNotification(
            Long userId,
            String title,
            String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId));

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    // =========================
    // GET MY NOTIFICATIONS
    // =========================

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications(
            Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // GET UNREAD NOTIFICATIONS
    // =========================

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotifications(
            Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                                userId);

        return notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // GET UNREAD COUNT
    // =========================

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    // =========================
    // MARK AS READ
    // =========================

    @Override
    @Transactional
    public void markAsRead(
            Long notificationId,
            Long userId) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found with id: "
                                                + notificationId));

        if (!notification.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You are not allowed to update this notification");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    // =========================
    // MARK ALL AS READ
    // =========================

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                                userId);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    // =========================
    // ENTITY → DTO
    // =========================

    private NotificationResponseDTO toDTO(
            Notification notification) {

        return new NotificationResponseDTO(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}