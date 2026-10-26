package com.uniassist.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_courses")
public class AcademicCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 150)
    private String courseName;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private Integer credits = 4;

    @Column(nullable = false)
    private Integer durationSemesters = 8;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AcademicCourse() {}

    public AcademicCourse(String courseCode, String courseName, String department,
                          Integer credits, Integer durationSemesters, String description) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.department = department;
        this.credits = credits;
        this.durationSemesters = durationSemesters;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Integer getDurationSemesters() { return durationSemesters; }
    public void setDurationSemesters(Integer durationSemesters) { this.durationSemesters = durationSemesters; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
