
package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ChatMessageRequestDTO;
import com.example.demo.dto.ChatMessageResponseDTO;
import com.example.demo.dto.ConversationResponseDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // SEND MESSAGE
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponseDTO> sendMessage(
            @RequestBody ChatMessageRequestDTO request,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        ChatMessageResponseDTO response =
                chatService.sendMessage(
                        request,
                        currentUser.getId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET MY CONVERSATIONS
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getMyConversations(
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        List<ConversationResponseDTO> conversations =
                chatService.getMyConversations(
                        currentUser.getId()
                );

        return ResponseEntity.ok(conversations);
    }

    // GET LATEST 30 MESSAGES
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessageResponseDTO>> getLatestMessages(
            @PathVariable Long conversationId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        List<ChatMessageResponseDTO> messages =
                chatService.getLatestMessages(
                        conversationId,
                        currentUser.getId()
                );

        return ResponseEntity.ok(messages);
    }

    // GET OLDER MESSAGES
    @GetMapping("/conversations/{conversationId}/messages/older")
    public ResponseEntity<List<ChatMessageResponseDTO>> getOlderMessages(
            @PathVariable Long conversationId,
            @RequestParam Long lastMessageId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        List<ChatMessageResponseDTO> messages =
                chatService.getOlderMessages(
                        conversationId,
                        currentUser.getId(),
                        lastMessageId
                );

        return ResponseEntity.ok(messages);
    }

    // MARK MESSAGES AS READ
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<String> markMessagesAsRead(
            @PathVariable Long conversationId,
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        chatService.markMessagesAsRead(
                conversationId,
                currentUser.getId()
        );

        return ResponseEntity.ok(
                "Messages marked as read successfully"
        );
    }

    // GET AVAILABLE USERS
    @GetMapping("/available-users")
    public ResponseEntity<List<UserResponseDTO>> getAvailableUsers(
            Authentication authentication) {

        User currentUser = getAuthenticatedUser(authentication);

        List<UserResponseDTO> users =
                chatService.getAvailableUsers(
                        currentUser.getId()
                );

        return ResponseEntity.ok(users);
    }

    // AUTHENTICATED USER
    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof User)) {

            throw new RuntimeException(
                    "Authenticated user not found"
            );
        }

        return (User) authentication.getPrincipal();
    }
}
