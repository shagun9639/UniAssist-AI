package com.uniassist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class TicketDtos {

    public static class CreateTicketRequest {
        @NotBlank(message = "Category is required")
        private String category;

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "Description is required")
        private String description;

        private String priority = "MEDIUM";

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class TicketResponseRequest {
        @NotBlank(message = "Response message is required")
        private String responseText;

        public String getResponseText() { return responseText; }
        public void setResponseText(String responseText) { this.responseText = responseText; }
    }

    public static class SupportTicketDto {
        private Long id;
        private Long userId;
        private String studentName;
        private String studentEmail;
        private String category;
        private String subject;
        private String description;
        private String status;
        private String priority;
        private String assignedTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<TicketResponseDto> responses;

        public SupportTicketDto(Long id, Long userId, String studentName, String studentEmail,
                                String category, String subject, String description,
                                String status, String priority, String assignedTo,
                                LocalDateTime createdAt, LocalDateTime updatedAt,
                                List<TicketResponseDto> responses) {
            this.id = id;
            this.userId = userId;
            this.studentName = studentName;
            this.studentEmail = studentEmail;
            this.category = category;
            this.subject = subject;
            this.description = description;
            this.status = status;
            this.priority = priority;
            this.assignedTo = assignedTo;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.responses = responses;
        }

        public Long getId() { return id; }
        public Long getUserId() { return userId; }
        public String getStudentName() { return studentName; }
        public String getStudentEmail() { return studentEmail; }
        public String getCategory() { return category; }
        public String getSubject() { return subject; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public String getAssignedTo() { return assignedTo; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public List<TicketResponseDto> getResponses() { return responses; }
    }

    public static class TicketResponseDto {
        private Long id;
        private Long responderId;
        private String responderName;
        private String responseText;
        private LocalDateTime createdAt;

        public TicketResponseDto(Long id, Long responderId, String responderName, String responseText, LocalDateTime createdAt) {
            this.id = id;
            this.responderId = responderId;
            this.responderName = responderName;
            this.responseText = responseText;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public Long getResponderId() { return responderId; }
        public String getResponderName() { return responderName; }
        public String getResponseText() { return responseText; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
