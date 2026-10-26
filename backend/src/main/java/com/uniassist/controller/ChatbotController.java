package com.uniassist.controller;

import com.uniassist.dto.ChatDtos.*;
import com.uniassist.security.UserPrincipal;
import com.uniassist.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Chatbot", description = "Student chat interactions, persisted conversation history, and feedback")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a message turn to the AI chatbot")
    public ResponseEntity<ChatMessageResponse> chat(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChatMessageRequest req) {
        return ResponseEntity.ok(chatbotService.processMessage(currentUser.getId(), req));
    }

    @GetMapping("/conversations")
    @Operation(summary = "List all chat sessions for the current student")
    public ResponseEntity<List<ConversationDto>> getConversations(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(chatbotService.getUserConversations(currentUser.getId()));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get full message transcript of a specific conversation")
    public ResponseEntity<List<MessageDto>> getConversationMessages(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(chatbotService.getConversationMessages(currentUser.getId(), id));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete a conversation and its message history")
    public ResponseEntity<Map<String, String>> deleteConversation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        chatbotService.deleteConversation(currentUser.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Conversation deleted successfully."));
    }

    @PostMapping("/chat/feedback")
    @Operation(summary = "Submit helpful/not-helpful rating and comment for a chatbot response")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody FeedbackRequestDto req) {
        chatbotService.recordFeedback(currentUser.getId(), req);
        return ResponseEntity.ok(Map.of("message", "Feedback recorded successfully."));
    }
}
