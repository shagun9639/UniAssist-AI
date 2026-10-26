package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false, length = 20)
    private String senderType; // STUDENT, ASSISTANT, SYSTEM

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(length = 100)
    private String predictedIntent;

    private Double modelScore;

    @Column(length = 255)
    private String sourceReference;

    private Boolean fallbackUsed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Message() {}

    public Message(Conversation conversation, String senderType, String messageText) {
        this.conversation = conversation;
        this.senderType = senderType;
        this.messageText = messageText;
    }

    public Message(Conversation conversation, String senderType, String messageText,
                   String predictedIntent, Double modelScore, String sourceReference, Boolean fallbackUsed) {
        this.conversation = conversation;
        this.senderType = senderType;
        this.messageText = messageText;
        this.predictedIntent = predictedIntent;
        this.modelScore = modelScore;
        this.sourceReference = sourceReference;
        this.fallbackUsed = fallbackUsed != null ? fallbackUsed : false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public String getPredictedIntent() { return predictedIntent; }
    public void setPredictedIntent(String predictedIntent) { this.predictedIntent = predictedIntent; }

    public Double getModelScore() { return modelScore; }
    public void setModelScore(Double modelScore) { this.modelScore = modelScore; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public Boolean getFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(Boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
