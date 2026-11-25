
package com.example.demo.mapper;

import org.springframework.stereotype.Component;


import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationResponseDTO;
import com.example.demo.entity.User;

@Component
public class NotificationMapper {

    public NotificationResponseDTO toResponseDTO(Notification notification) {

        if (notification == null) {
            return null;
        }

        NotificationResponseDTO dto = new NotificationResponseDTO();

        dto.setId(notification.getId());

        // Actor information
        User actor = notification.getActor();

        if (actor != null) {
            dto.setActorId(actor.getId());

            String firstName = actor.getFirstName();
            String lastName = actor.getLastName();

            String actorName = (
                (firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "")
            ).trim();

            dto.setActorName(actorName);
        }

        dto.setType(notification.getType());
        dto.setCategory(notification.getCategory());
        dto.setPriority(notification.getPriority());

        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());

        dto.setReferenceType(notification.getReferenceType());
        dto.setReferenceId(notification.getReferenceId());

        dto.setActionUrl(notification.getActionUrl());

        dto.setRead(notification.isRead());
        dto.setReadAt(notification.getReadAt());

        dto.setArchived(notification.isArchived());

        dto.setCreatedAt(notification.getCreatedAt());
        dto.setExpiresAt(notification.getExpiresAt());

        return dto;
    }
}
