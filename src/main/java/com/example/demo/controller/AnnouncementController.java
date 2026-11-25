
package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.AnnouncementRequestDTO;
import com.example.demo.dto.AnnouncementResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.AnnouncementService;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(
            AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // ADMIN: Send broadcast announcement
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnouncementResponseDTO> createAnnouncement(
            @RequestBody AnnouncementRequestDTO request,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        AnnouncementResponseDTO response =
                announcementService.createAnnouncement(
                        request, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    // Get logged-in user's announcements
    @GetMapping
    public ResponseEntity<List<AnnouncementResponseDTO>> getMyAnnouncements(
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                announcementService.getMyAnnouncements(
                        currentUser.getId()));
    }

    // Mark announcement as read
    @PutMapping("/{announcementId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long announcementId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        announcementService.markAsRead(
                announcementId, currentUser.getId());

        return ResponseEntity.ok(
                Map.of("message", "Announcement marked as read"));
    }

    // Get unread announcement count
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        long count = announcementService.getUnreadCount(
                currentUser.getId());

        return ResponseEntity.ok(
                Map.of("unreadCount", count));
    }
}
