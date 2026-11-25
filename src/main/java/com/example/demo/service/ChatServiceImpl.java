
package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.service.NotificationService;
import com.example.demo.dto.ChatMessageRequestDTO;
import com.example.demo.dto.ChatMessageResponseDTO;
import com.example.demo.dto.ConversationResponseDTO;
import com.example.demo.dto.UserResponseDTO;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.User;

import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.ConversationMapper;

import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import com.example.demo.entity.NotificationRequestDTO;
import com.example.demo.entity.NotificationResponseDTO;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.NotificationCategory;
import com.example.demo.entity.NotificationPriority;
@Service
public class ChatServiceImpl implements ChatService {
	private final NotificationService notificationService;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    private final ChatMessageMapper chatMessageMapper;
    private final ConversationMapper conversationMapper;

    public ChatServiceImpl(
            ChatMessageRepository chatMessageRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            ChatMessageMapper chatMessageMapper,
            ConversationMapper conversationMapper,
            NotificationService notificationService) {

        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.chatMessageMapper = chatMessageMapper;
        this.conversationMapper = conversationMapper;
        this.notificationService = notificationService;
    }

    // ==========================================
    // SEND MESSAGE
    // ==========================================

    @Override
    @Transactional
    public ChatMessageResponseDTO sendMessage(
            ChatMessageRequestDTO request,
            Long senderId) {

        if (request == null || request.getReceiverId() == null) {
            throw new RuntimeException("Receiver ID is required");
        }

        if (request.getMessage() == null ||
                request.getMessage().isBlank()) {
            throw new RuntimeException("Message cannot be empty");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() ->
                        new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(
                request.getReceiverId()
        ).orElseThrow(() ->
                new RuntimeException("Receiver not found"));

        validateChatPermission(sender, receiver);

        Long userOneId = Math.min(
                sender.getId(),
                receiver.getId()
        );

        Long userTwoId = Math.max(
                sender.getId(),
                receiver.getId()
        );

        Conversation conversation = conversationRepository
                .findByUserOne_IdAndUserTwo_Id(
                        userOneId,
                        userTwoId
                )
                .orElseGet(() -> {

                    Conversation newConversation =
                            new Conversation();

                    newConversation.setUserOne(
                            userOneId.equals(sender.getId())
                                    ? sender : receiver
                    );

                    newConversation.setUserTwo(
                            userTwoId.equals(sender.getId())
                                    ? sender : receiver
                    );

                    return conversationRepository.save(
                            newConversation
                    );
                });

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setConversation(conversation);
        chatMessage.setSender(sender);
        chatMessage.setMessage(request.getMessage().trim());
        chatMessage.setRead(false);

        // Save Chat Message
        ChatMessage savedMessage =
                chatMessageRepository.save(chatMessage);

        // Create Notification
        NotificationRequestDTO notification =
                new NotificationRequestDTO();

        notification.setType(NotificationType.CHAT_MESSAGE);
        notification.setCategory(NotificationCategory.COMMUNICATION);
        notification.setPriority(NotificationPriority.NORMAL);

        notification.setTitle("New Chat Message");

        notification.setMessage(
                sender.getFirstName() + " " + sender.getLastName()
                + " sent you a message."
        );

        notification.setReferenceType("CHAT");
        notification.setReferenceId(conversation.getId());
        notification.setActionUrl("/chat");

        notificationService.createNotification(
                receiver.getId(),
                sender.getId(),
                notification
        );
        System.out.println("========== CHAT NOTIFICATION START ==========");
        System.out.println("Sender ID: " + sender.getId());
        System.out.println("Receiver ID: " + receiver.getId());

        NotificationResponseDTO savedNotification =
                notificationService.createNotification(
                        receiver.getId(),
                        sender.getId(),
                        notification
                );

        System.out.println("Notification Saved ID: " + savedNotification.getId());
        System.out.println("========== CHAT NOTIFICATION END ==========");

        // Return response
        return chatMessageMapper.toResponseDTO(
                savedMessage,
                receiver
        );    }

    // ==========================================
    // CHAT PERMISSION
    // ==========================================

    private void validateChatPermission(
            User sender,
            User receiver) {

        if (!"ACTIVE".equalsIgnoreCase(sender.getStatus())) {
            throw new RuntimeException(
                    "Inactive sender cannot use chat"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(receiver.getStatus())) {
            throw new RuntimeException(
                    "You cannot chat with an inactive user"
            );
        }

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException(
                    "You cannot message yourself"
            );
        }
    }

    // ==========================================
    // GET MY CONVERSATIONS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponseDTO> getMyConversations(
            Long userId) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return conversationRepository
                .findByUserOne_IdOrUserTwo_IdOrderByCreatedAtDesc(
                        userId,
                        userId
                )
                .stream()
                .map(conversation -> {

                    ChatMessage lastMessage =
                            chatMessageRepository
                                    .findTopByConversation_IdOrderBySentAtDescIdDesc(
                                            conversation.getId()
                                    )
                                    .orElse(null);

                    long unreadCount =
                            chatMessageRepository
                                    .countByConversation_IdAndSender_IdNotAndIsReadFalse(
                                            conversation.getId(),
                                            userId
                                    );

                    return conversationMapper.toResponseDTO(
                            conversation,
                            currentUser,
                            lastMessage != null
                                    ? lastMessage.getMessage()
                                    : null,
                            lastMessage != null
                                    ? lastMessage.getSentAt()
                                    : null,
                            unreadCount
                    );

                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET CHAT MESSAGES
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getMessages(
            Long conversationId,
            Long userId) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Conversation not found"
                                ));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        User otherUser;

        if (conversation.getUserOne().getId().equals(userId)) {

            otherUser = conversation.getUserTwo();

        } else if (conversation.getUserTwo().getId().equals(userId)) {

            otherUser = conversation.getUserOne();

        } else {

            throw new RuntimeException(
                    "You are not a participant in this conversation"
            );
        }

        return chatMessageRepository
                .findByConversation_IdOrderBySentAtAsc(
                        conversationId
                )
                .stream()
                .map(message -> {

                    User receiver =
                            message.getSender().getId().equals(userId)
                                    ? otherUser
                                    : currentUser;

                    return chatMessageMapper.toResponseDTO(
                            message,
                            receiver
                    );
                })
                .collect(Collectors.toList());
    }

    // ==========================================
    // MARK MESSAGES AS READ
    // ==========================================

    @Override
    @Transactional
    public void markMessagesAsRead(
            Long conversationId,
            Long userId) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Conversation not found"
                                ));

        boolean isParticipant =
                conversation.getUserOne().getId().equals(userId)
                || conversation.getUserTwo().getId().equals(userId);

        if (!isParticipant) {
            throw new RuntimeException(
                    "You are not a participant in this conversation"
            );
        }

        List<ChatMessage> unreadMessages =
                chatMessageRepository
                        .findByConversation_IdAndSender_IdNotAndIsReadFalse(
                                conversationId,
                                userId
                        );

        LocalDateTime readTime = LocalDateTime.now();

        for (ChatMessage message : unreadMessages) {

            message.setRead(true);
            message.setReadAt(readTime);
        }

        chatMessageRepository.saveAll(unreadMessages);
    }

    // ==========================================
    // GET AVAILABLE USERS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAvailableUsers(
            Long currentUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"ACTIVE".equalsIgnoreCase(currentUser.getStatus())) {
            throw new RuntimeException(
                    "Inactive users cannot use chat"
            );
        }

        List<User> availableUsers =
                userRepository.findAll();

        return availableUsers.stream()

                .filter(user ->
                        !user.getId().equals(currentUserId)
                )

                .filter(user ->
                        "ACTIVE".equalsIgnoreCase(user.getStatus())
                )

                .map(user -> {

                    UserResponseDTO dto =
                            new UserResponseDTO();

                    dto.setId(user.getId());

                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());

                    dto.setEmail(user.getEmail());
                    dto.setMobile(user.getMobile());

                    dto.setStatus(user.getStatus());
                    dto.setRole(user.getRole().name());

                    return dto;
                })

                .collect(Collectors.toList());
    }

    // ==========================================
    // GET LATEST 30 MESSAGES
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getLatestMessages(
            Long conversationId,
            Long userId) {

        Conversation conversation =
                validateConversationParticipant(conversationId, userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        User otherUser = conversation.getUserOne()
                .getId().equals(userId)
                ? conversation.getUserTwo()
                : conversation.getUserOne();

        Pageable pageable = PageRequest.of(0, 30);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByConversation_IdOrderBySentAtDescIdDesc(
                                conversationId,
                                pageable
                        );

        return mapMessages(messages, userId, currentUser, otherUser);
    }

    // ==========================================
    // GET OLDER 30 MESSAGES
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getOlderMessages(
            Long conversationId,
            Long userId,
            Long lastMessageId) {

        if (lastMessageId == null || lastMessageId <= 0) {
            throw new RuntimeException("Invalid last message ID");
        }

        Conversation conversation =
                validateConversationParticipant(conversationId, userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        User otherUser = conversation.getUserOne()
                .getId().equals(userId)
                ? conversation.getUserTwo()
                : conversation.getUserOne();

        Pageable pageable = PageRequest.of(0, 30);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByConversation_IdAndIdLessThanOrderBySentAtDescIdDesc(
                                conversationId,
                                lastMessageId,
                                pageable
                        );

        return mapMessages(messages, userId, currentUser, otherUser);
    }

    // ==========================================
    // VALIDATE CONVERSATION PARTICIPANT
    // ==========================================

    private Conversation validateConversationParticipant(
            Long conversationId,
            Long userId) {

        Conversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new RuntimeException("Conversation not found"));

        boolean isParticipant =
                conversation.getUserOne().getId().equals(userId)
                || conversation.getUserTwo().getId().equals(userId);

        if (!isParticipant) {
            throw new RuntimeException(
                    "You are not a participant in this conversation"
            );
        }

        return conversation;
    }

    // ==========================================
    // MAP CHAT MESSAGES
    // ==========================================

    private List<ChatMessageResponseDTO> mapMessages(
            List<ChatMessage> messages,
            Long userId,
            User currentUser,
            User otherUser) {

        Collections.reverse(messages);

        return messages.stream()
                .map(message -> {

                    User receiver =
                            message.getSender().getId().equals(userId)
                                    ? otherUser
                                    : currentUser;

                    return chatMessageMapper.toResponseDTO(
                            message,
                            receiver
                    );
                })
                .collect(Collectors.toList());
    }

}
