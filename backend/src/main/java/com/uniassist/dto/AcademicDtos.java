package com.uniassist.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AcademicDtos {

    public static class CourseDto {
        private Long id;
        private String courseCode;
        private String courseName;
        private String department;
        private Integer credits;
        private Integer durationSemesters;
        private String description;

        public CourseDto(Long id, String courseCode, String courseName, String department,
                         Integer credits, Integer durationSemesters, String description) {
            this.id = id;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.department = department;
            this.credits = credits;
            this.durationSemesters = durationSemesters;
            this.description = description;
        }

        public Long getId() { return id; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getDepartment() { return department; }
        public Integer getCredits() { return credits; }
        public Integer getDurationSemesters() { return durationSemesters; }
        public String getDescription() { return description; }
    }

    public static class EventDto {
        private Long id;
        private String title;
        private String eventType;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private String semester;

        public EventDto(Long id, String title, String eventType, LocalDate startDate,
                        LocalDate endDate, String description, String semester) {
            this.id = id;
            this.title = title;
            this.eventType = eventType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.description = description;
            this.semester = semester;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getEventType() { return eventType; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public String getDescription() { return description; }
        public String getSemester() { return semester; }
    }

    public static class AnnouncementDto {
        private Long id;
        private String title;
        private String description;
        private String category;
        private LocalDateTime publishedAt;
        private LocalDate expiryDate;
        private Boolean active;

        public AnnouncementDto() {}

        public AnnouncementDto(Long id, String title, String description, String category,
                               LocalDateTime publishedAt, LocalDate expiryDate, Boolean active) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.category = category;
            this.publishedAt = publishedAt;
            this.expiryDate = expiryDate;
            this.active = active;
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
}
