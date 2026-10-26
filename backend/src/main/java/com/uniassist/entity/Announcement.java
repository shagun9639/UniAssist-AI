package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category = "General";

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private Boolean active = true;

    public Announcement() {}

    public Announcement(String title, String description, String category, LocalDate expiryDate) {
        this.title = title;
        this.description = description;
        this.category = category != null ? category : "General";
        this.expiryDate = expiryDate;
        this.publishedAt = LocalDateTime.now();
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
