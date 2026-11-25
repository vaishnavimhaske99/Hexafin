
package com.example.demo.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationResponseDTO;
import com.example.demo.service.NotificationService;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all active notifications
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                notificationService.getNotifications(userId, pageable)
        );
    }

    // Get unread notifications
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId, pageable)
        );
    }

    // Get unread notification count
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadCount(userId)
        );
    }

    // Get notifications by category
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByCategory(
            @RequestParam Long userId,
            @PathVariable NotificationCategory category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                notificationService.getNotificationsByCategory(
                        userId, category, pageable)
        );
    }

    // Mark one notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.markAsRead(notificationId, userId)
        );
    }

    // Mark all notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<Integer> markAllAsRead(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                notificationService.markAllAsRead(userId)
        );
    }

    // Archive notification
    @PutMapping("/{notificationId}/archive")
    public ResponseEntity<Void> archiveNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {

        notificationService.archiveNotification(notificationId, userId);

        return ResponseEntity.noContent().build();
    }

    // Get archived notifications
    @GetMapping("/archived")
    public ResponseEntity<Page<NotificationResponseDTO>> getArchivedNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                notificationService.getArchivedNotifications(userId, pageable)
        );
    }
}
