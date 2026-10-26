package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "unanswered_queries")
public class UnansweredQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(length = 100)
    private String predictedIntent;

    private Double modelScore;

    @Column(length = 30)
    private String reviewStatus = "UNREVIEWED"; // UNREVIEWED, RESOLVED_AS_FAQ, DISMISSED

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UnansweredQuery() {}

    public UnansweredQuery(String queryText, String predictedIntent, Double modelScore) {
        this.queryText = queryText;
        this.predictedIntent = predictedIntent;
        this.modelScore = modelScore;
        this.reviewStatus = "UNREVIEWED";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    public String getPredictedIntent() { return predictedIntent; }
    public void setPredictedIntent(String predictedIntent) { this.predictedIntent = predictedIntent; }

    public Double getModelScore() { return modelScore; }
    public void setModelScore(Double modelScore) { this.modelScore = modelScore; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
