
package com.example.demo.mapper;

import org.springframework.stereotype.Component;

import com.example.demo.dto.ChatMessageResponseDTO;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.User;

@Component
public class ChatMessageMapper {

    public ChatMessageResponseDTO toResponseDTO(
            ChatMessage chatMessage,
            User receiver) {

        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();

        Conversation conversation = chatMessage.getConversation();
        User sender = chatMessage.getSender();

        dto.setId(chatMessage.getId());
        dto.setConversationId(conversation.getId());

        dto.setSenderId(sender.getId());
        dto.setSenderName(
                sender.getFirstName() + " " + sender.getLastName()
        );

        dto.setReceiverId(receiver.getId());
        dto.setReceiverName(
                receiver.getFirstName() + " " + receiver.getLastName()
        );

        dto.setMessage(chatMessage.getMessage());
        dto.setSentAt(chatMessage.getSentAt());
        dto.setRead(chatMessage.isRead());
        dto.setReadAt(chatMessage.getReadAt());

        return dto;
    }
}
