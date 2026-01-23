package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.chat.ChatMessageRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.chat.ChatMessageResponse;
import com.example.ecommerce.ecommerce_backend.application.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> send(Authentication auth, @RequestBody ChatMessageRequest request) {
        Long senderId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(chatService.sendMessage(senderId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(
            Authentication auth,
            @RequestParam Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(chatService.getHistory(userId, recipientId, PageRequest.of(page, size)));
    }
    
    @GetMapping("/conversations")
    public ResponseEntity<List<com.example.ecommerce.ecommerce_backend.api.dto.chat.ConversationResponse>> getConversations(
            Authentication auth
    ) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(chatService.getConversations(userId));
    }
}
