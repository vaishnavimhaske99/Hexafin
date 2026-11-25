
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Existing method: All messages in ascending order
    List<ChatMessage> findByConversation_IdOrderBySentAtAsc(
            Long conversationId
    );

    // Latest message in a conversation
    Optional<ChatMessage> findTopByConversation_IdOrderBySentAtDescIdDesc(
            Long conversationId
    );

    // Latest messages with pagination
    List<ChatMessage> findByConversation_IdOrderBySentAtDescIdDesc(
            Long conversationId,
            Pageable pageable
    );

    // Older messages with pagination
    List<ChatMessage> findByConversation_IdAndIdLessThanOrderBySentAtDescIdDesc(
            Long conversationId,
            Long lastMessageId,
            Pageable pageable
    );

    // Find unread messages received by current user
    List<ChatMessage> findByConversation_IdAndSender_IdNotAndIsReadFalse(
            Long conversationId,
            Long currentUserId
    );

    // Count unread messages
    long countByConversation_IdAndSender_IdNotAndIsReadFalse(
            Long conversationId,
            Long currentUserId
    );
}
