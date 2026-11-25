
package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.NotificationPriority;
import com.example.demo.entity.NotificationRequestDTO;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
    }

    // Create notification
    @Override
    @Transactional
    public NotificationResponseDTO createNotification(
            Long recipientId,
            Long actorId,
            NotificationRequestDTO request) {

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Notification recipient not found: " + recipientId));

        User actor = null;

        if (actorId != null) {
            actor = userRepository.findById(actorId)
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Notification actor not found: " + actorId));
        }

        Notification notification = new Notification();

        notification.setUser(recipient);
        notification.setActor(actor);

        notification.setType(request.getType());
        notification.setCategory(request.getCategory());

        notification.setPriority(
                request.getPriority() != null
                        ? request.getPriority()
                        : NotificationPriority.NORMAL
        );

        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());

        notification.setReferenceType(request.getReferenceType());
        notification.setReferenceId(request.getReferenceId());
        notification.setActionUrl(request.getActionUrl());

        Notification savedNotification =
                notificationRepository.save(notification);

        return notificationMapper.toResponseDTO(savedNotification);
    }

    // Get all active notifications
    @Override
    public Page<NotificationResponseDTO> getNotifications(
            Long userId,
            Pageable pageable) {

        return notificationRepository
                .findByUser_IdAndIsArchivedFalseOrderByCreatedAtDesc(
                        userId, pageable)
                .map(notificationMapper::toResponseDTO);
    }

    // Get unread notifications
    @Override
    public Page<NotificationResponseDTO> getUnreadNotifications(
            Long userId,
            Pageable pageable) {

        return notificationRepository
                .findByUser_IdAndIsReadFalseAndIsArchivedFalseOrderByCreatedAtDesc(
                        userId, pageable)
                .map(notificationMapper::toResponseDTO);
    }

    // Get unread count
    @Override
    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUser_IdAndIsReadFalseAndIsArchivedFalse(userId);
    }

    // Get notifications by category
    @Override
    public Page<NotificationResponseDTO> getNotificationsByCategory(
            Long userId,
            com.example.demo.entity.NotificationCategory category,
            Pageable pageable) {

        return notificationRepository
                .findByUser_IdAndCategoryAndIsArchivedFalseOrderByCreatedAtDesc(
                        userId, category, pageable)
                .map(notificationMapper::toResponseDTO);
    }

    // Mark one notification as read
    @Override
    @Transactional
    public NotificationResponseDTO markAsRead(
            Long notificationId,
            Long userId) {

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Notification not found: " + notificationId));

        notification.setRead(true);

        Notification updatedNotification =
                notificationRepository.save(notification);

        return notificationMapper.toResponseDTO(updatedNotification);
    }

    // Mark all notifications as read
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {

        return notificationRepository.markAllAsRead(userId);
    }

    // Archive notification
    @Override
    @Transactional
    public void archiveNotification(
            Long notificationId,
            Long userId) {

        int updatedRows = notificationRepository.archiveNotification(
                notificationId, userId);

        if (updatedRows == 0) {
            throw new EntityNotFoundException(
                    "Notification not found for this user: " + notificationId);
        }
    }

    // Get archived notifications
    @Override
    public Page<NotificationResponseDTO> getArchivedNotifications(
            Long userId,
            Pageable pageable) {

        return notificationRepository
                .findByUser_IdAndIsArchivedTrueOrderByCreatedAtDesc(
                        userId, pageable)
                .map(notificationMapper::toResponseDTO);
    }
}
