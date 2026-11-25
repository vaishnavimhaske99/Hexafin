
package com.example.demo.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.dto.AnnouncementResponseDTO;
import com.example.demo.entity.Announcement;
import com.example.demo.entity.AnnouncementRecipient;

@Component
public class AnnouncementMapper {

    public AnnouncementResponseDTO toResponseDTO(
            Announcement announcement,
            AnnouncementRecipient recipient) {

        AnnouncementResponseDTO dto = new AnnouncementResponseDTO();

        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setMessage(announcement.getMessage());

        dto.setCreatedById(announcement.getCreatedBy().getId());

        String adminName =
                announcement.getCreatedBy().getFirstName()
                + " "
                + announcement.getCreatedBy().getLastName();

        dto.setCreatedByName(adminName.trim());
        dto.setCreatedAt(announcement.getCreatedAt());

        if (recipient != null) {
            dto.setRead(recipient.isRead());
            dto.setReadAt(recipient.getReadAt());
        } else {
            dto.setRead(false);
            dto.setReadAt(null);
        }

        return dto;
    }
}
