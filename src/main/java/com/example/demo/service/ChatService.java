
package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.ChatMessageRequestDTO;
import com.example.demo.dto.ChatMessageResponseDTO;
import com.example.demo.dto.ConversationResponseDTO;
import com.example.demo.dto.UserResponseDTO;

public interface ChatService {

    ChatMessageResponseDTO sendMessage(
            ChatMessageRequestDTO request,
            Long senderId
    );

    List<ConversationResponseDTO> getMyConversations(
            Long userId
    );

    List<ChatMessageResponseDTO> getMessages(
            Long conversationId,
            Long userId
    );

    void markMessagesAsRead(
            Long conversationId,
            Long userId
    );
    List<ChatMessageResponseDTO> getLatestMessages(
            Long conversationId,
            Long userId
    );

    List<ChatMessageResponseDTO> getOlderMessages(
            Long conversationId,
            Long userId,
            Long lastMessageId
    );

    List<UserResponseDTO> getAvailableUsers(
            Long currentUserId
    );
}
