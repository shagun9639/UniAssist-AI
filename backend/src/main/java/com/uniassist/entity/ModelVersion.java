package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "model_versions")
public class ModelVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false, length = 30)
    private String version;

    @Column(nullable = false, length = 255)
    private String modelPath;

    @Column(columnDefinition = "TEXT")
    private String evaluationSummary;

    @Column(length = 100)
    private String approvedBy = "System";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ModelVersion() {}

    public ModelVersion(String modelName, String version, String modelPath, String evaluationSummary, String approvedBy) {
        this.modelName = modelName;
        this.version = version;
        this.modelPath = modelPath;
        this.evaluationSummary = evaluationSummary;
        this.approvedBy = approvedBy != null ? approvedBy : "System";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getModelPath() { return modelPath; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }

    public String getEvaluationSummary() { return evaluationSummary; }
    public void setEvaluationSummary(String evaluationSummary) { this.evaluationSummary = evaluationSummary; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
