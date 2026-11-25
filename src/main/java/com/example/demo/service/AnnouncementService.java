
package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.AnnouncementRequestDTO;
import com.example.demo.dto.AnnouncementResponseDTO;

public interface AnnouncementService {

    AnnouncementResponseDTO createAnnouncement(
            AnnouncementRequestDTO request,
            Long adminId
    );

    List<AnnouncementResponseDTO> getMyAnnouncements(Long userId);

    void markAsRead(Long announcementId, Long userId);

    long getUnreadCount(Long userId);
}
