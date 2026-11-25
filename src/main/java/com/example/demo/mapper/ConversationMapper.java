
package com.example.demo.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.dto.ConversationResponseDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.User;

@Component
public class ConversationMapper {

    public ConversationResponseDTO toResponseDTO(
            Conversation conversation,
            User currentUser,
            String lastMessage,
            java.time.LocalDateTime lastMessageAt,
            long unreadCount) {

        ConversationResponseDTO dto = new ConversationResponseDTO();

        dto.setId(conversation.getId());

        User participant =
                conversation.getUserOne().getId().equals(currentUser.getId())
                ? conversation.getUserTwo()
                : conversation.getUserOne();

        dto.setParticipantId(participant.getId());

        dto.setParticipantName(
                participant.getFirstName() + " "
                + participant.getLastName()
        );

        dto.setParticipantRole(
                participant.getRole().name()
        );

        dto.setLastMessage(lastMessage);
        dto.setLastMessageAt(lastMessageAt);
        dto.setUnreadCount(unreadCount);

        return dto;
    }
}
