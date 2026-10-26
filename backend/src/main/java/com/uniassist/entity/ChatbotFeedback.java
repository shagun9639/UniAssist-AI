package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_feedback")
public class ChatbotFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(nullable = false, length = 20)
    private String rating; // HELPFUL, NOT_HELPFUL

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(length = 30)
    private String reviewStatus = "PENDING"; // PENDING, REVIEWED, APPROVED_FOR_TRAINING

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatbotFeedback() {}

    public ChatbotFeedback(User user, Message message, String rating, String comments) {
        this.user = user;
        this.message = message;
        this.rating = rating;
        this.comments = comments;
        this.reviewStatus = "PENDING";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
