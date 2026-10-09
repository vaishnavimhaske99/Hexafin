package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.NotificationResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // =====================================================
    // GET MY NOTIFICATIONS
    // =====================================================

    @GetMapping
    public List<NotificationResponseDTO> getMyNotifications(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return notificationService.getMyNotifications(
                user.getId()
        );
    }


    // =====================================================
    // GET MY UNREAD NOTIFICATIONS
    // =====================================================

    @GetMapping("/unread")
    public List<NotificationResponseDTO> getUnreadNotifications(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return notificationService.getUnreadNotifications(
                user.getId()
        );
    }


    // =====================================================
    // GET UNREAD COUNT
    // =====================================================

    @GetMapping("/unread-count")
    public long getUnreadCount(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return notificationService.getUnreadCount(
                user.getId()
        );
    }


    // =====================================================
    // MARK ONE AS READ
    // =====================================================

    @PutMapping("/{notificationId}/read")
    public String markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        notificationService.markAsRead(
                notificationId,
                user.getId()
        );

        return "Notification marked as read";
    }


    // =====================================================
    // MARK ALL AS READ
    // =====================================================

    @PutMapping("/read-all")
    public String markAllAsRead(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        notificationService.markAllAsRead(
                user.getId()
        );

        return "All notifications marked as read";
    }
}