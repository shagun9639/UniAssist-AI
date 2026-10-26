package com.uniassist.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminDtos {

    public static class AdminDashboardStatsDto {
        private long totalStudents;
        private long totalConversations;
        private long totalMessages;
        private long openTickets;
        private long totalFaqs;
        private long unansweredQueries;
        private List<IntentStatDto> topIntents;
        private Map<String, Long> ticketStatusBreakdown;

        public AdminDashboardStatsDto(long totalStudents, long totalConversations, long totalMessages,
                                      long openTickets, long totalFaqs, long unansweredQueries,
                                      List<IntentStatDto> topIntents, Map<String, Long> ticketStatusBreakdown) {
            this.totalStudents = totalStudents;
            this.totalConversations = totalConversations;
            this.totalMessages = totalMessages;
            this.openTickets = openTickets;
            this.totalFaqs = totalFaqs;
            this.unansweredQueries = unansweredQueries;
            this.topIntents = topIntents;
            this.ticketStatusBreakdown = ticketStatusBreakdown;
        }

        public long getTotalStudents() { return totalStudents; }
        public long getTotalConversations() { return totalConversations; }
        public long getTotalMessages() { return totalMessages; }
        public long getOpenTickets() { return openTickets; }
        public long getTotalFaqs() { return totalFaqs; }
        public long getUnansweredQueries() { return unansweredQueries; }
        public List<IntentStatDto> getTopIntents() { return topIntents; }
        public Map<String, Long> getTicketStatusBreakdown() { return ticketStatusBreakdown; }
    }

    public static class IntentStatDto {
        private String intent;
        private long count;

        public IntentStatDto(String intent, long count) {
            this.intent = intent;
            this.count = count;
        }

        public String getIntent() { return intent; }
        public long getCount() { return count; }
    }

    public static class UnansweredQueryDto {
        private Long id;
        private String queryText;
        private String predictedIntent;
        private Double modelScore;
        private String reviewStatus;
        private LocalDateTime createdAt;

        public UnansweredQueryDto(Long id, String queryText, String predictedIntent,
                                  Double modelScore, String reviewStatus, LocalDateTime createdAt) {
            this.id = id;
            this.queryText = queryText;
            this.predictedIntent = predictedIntent;
            this.modelScore = modelScore;
            this.reviewStatus = reviewStatus;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getQueryText() { return queryText; }
        public String getPredictedIntent() { return predictedIntent; }
        public Double getModelScore() { return modelScore; }
        public String getReviewStatus() { return reviewStatus; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}
