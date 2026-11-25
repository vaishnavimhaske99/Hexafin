
package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Conversation;

@Repository
public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    // Find conversation between two users
    Optional<Conversation> findByUserOne_IdAndUserTwo_Id(
            Long userOneId, Long userTwoId);

    // Get all conversations of a user
    List<Conversation> findByUserOne_IdOrUserTwo_IdOrderByCreatedAtDesc(
            Long userOneId, Long userTwoId);
}
