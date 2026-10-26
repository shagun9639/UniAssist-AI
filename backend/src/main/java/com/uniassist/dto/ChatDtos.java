package com.uniassist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ChatDtos {

    public static class ChatMessageRequest {
        private Long conversationId;

        @NotBlank(message = "Message text cannot be empty")
        private String message;

        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ChatMessageResponse {
        private Long conversationId;
        private Long messageId;
        private String reply;
        private String intent;
        private Double confidence;
        private String source;
        private Boolean fallback;
        private Map<String, Object> entities;
        private List<String> suggestedFollowups;
        private LocalDateTime timestamp;

        public ChatMessageResponse() {}

        public ChatMessageResponse(Long conversationId, Long messageId, String reply, String intent,
                                   Double confidence, String source, Boolean fallback,
                                   Map<String, Object> entities, List<String> suggestedFollowups) {
            this.conversationId = conversationId;
            this.messageId = messageId;
            this.reply = reply;
            this.intent = intent;
            this.confidence = confidence;
            this.source = source;
            this.fallback = fallback;
            this.entities = entities;
            this.suggestedFollowups = suggestedFollowups;
            this.timestamp = LocalDateTime.now();
        }

        public Long getConversationId() { return conversationId; }
        public Long getMessageId() { return messageId; }
        public String getReply() { return reply; }
        public String getIntent() { return intent; }
        public Double getConfidence() { return confidence; }
        public String getSource() { return source; }
        public Boolean getFallback() { return fallback; }
        public Map<String, Object> getEntities() { return entities; }
        public List<String> getSuggestedFollowups() { return suggestedFollowups; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class ConversationDto {
        private Long id;
        private String title;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int messageCount;

        public ConversationDto(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt, int messageCount) {
            this.id = id;
            this.title = title;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.messageCount = messageCount;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public int getMessageCount() { return messageCount; }
    }

    public static class MessageDto {
        private Long id;
        private String senderType;
        private String messageText;
        private String predictedIntent;
        private Double modelScore;
        private String sourceReference;
        private Boolean fallbackUsed;
        private LocalDateTime createdAt;

        public MessageDto(Long id, String senderType, String messageText, String predictedIntent,
                          Double modelScore, String sourceReference, Boolean fallbackUsed, LocalDateTime createdAt) {
            this.id = id;
            this.senderType = senderType;
            this.messageText = messageText;
            this.predictedIntent = predictedIntent;
            this.modelScore = modelScore;
            this.sourceReference = sourceReference;
            this.fallbackUsed = fallbackUsed;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getSenderType() { return senderType; }
        public String getMessageText() { return messageText; }
        public String getPredictedIntent() { return predictedIntent; }
        public Double getModelScore() { return modelScore; }
        public String getSourceReference() { return sourceReference; }
        public Boolean getFallbackUsed() { return fallbackUsed; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class FeedbackRequestDto {
        private Long messageId;
        private String rating; // HELPFUL, NOT_HELPFUL
        private String comments;

        public Long getMessageId() { return messageId; }
        public void setMessageId(Long messageId) { this.messageId = messageId; }

        public String getRating() { return rating; }
        public void setRating(String rating) { this.rating = rating; }

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
}
