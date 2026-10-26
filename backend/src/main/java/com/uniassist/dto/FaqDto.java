package com.uniassist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class FaqDto {
    private Long id;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    @NotBlank(message = "Category is required")
    private String category;

    private String keywords;
    private String sourceUrl;
    private Boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FaqDto() {}

    public FaqDto(Long id, String question, String answer, String category,
                  String keywords, String sourceUrl, Boolean active,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.keywords = keywords;
        this.sourceUrl = sourceUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
