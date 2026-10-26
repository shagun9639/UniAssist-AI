package com.uniassist.service;

import com.uniassist.client.AiServiceClient;
import com.uniassist.dto.AdminDtos.*;
import com.uniassist.dto.AuthDtos.UserProfileDto;
import com.uniassist.dto.ModelInfoResponse;
import com.uniassist.entity.ChatbotFeedback;
import com.uniassist.entity.UnansweredQuery;
import com.uniassist.entity.User;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final FaqRepository faqRepository;
    private final UnansweredQueryRepository unansweredQueryRepository;
    private final ChatbotFeedbackRepository chatbotFeedbackRepository;
    private final AiServiceClient aiServiceClient;

    public AdminService(UserRepository userRepository,
                        ConversationRepository conversationRepository,
                        MessageRepository messageRepository,
                        SupportTicketRepository supportTicketRepository,
                        FaqRepository faqRepository,
                        UnansweredQueryRepository unansweredQueryRepository,
                        ChatbotFeedbackRepository chatbotFeedbackRepository,
                        AiServiceClient aiServiceClient) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.faqRepository = faqRepository;
        this.unansweredQueryRepository = unansweredQueryRepository;
        this.chatbotFeedbackRepository = chatbotFeedbackRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getDashboardStats() {
        long totalStudents = userRepository.countByRole("ROLE_STUDENT");
        long totalConversations = conversationRepository.count();
        long totalMessages = messageRepository.count();
        long openTickets = supportTicketRepository.countByStatus("OPEN");
        long totalFaqs = faqRepository.countByActiveTrue();
        long unansweredCount = unansweredQueryRepository.countByReviewStatus("UNREVIEWED");

        List<Object[]> rawTopIntents = messageRepository.findTopIntents();
        List<IntentStatDto> topIntents = new ArrayList<>();
        int limit = 0;
        for (Object[] row : rawTopIntents) {
            if (limit++ >= 6) break;
            topIntents.add(new IntentStatDto((String) row[0], ((Number) row[1]).longValue()));
        }

        Map<String, Long> ticketStatusMap = new HashMap<>();
        ticketStatusMap.put("OPEN", supportTicketRepository.countByStatus("OPEN"));
        ticketStatusMap.put("IN_PROGRESS", supportTicketRepository.countByStatus("IN_PROGRESS"));
        ticketStatusMap.put("RESOLVED", supportTicketRepository.countByStatus("RESOLVED"));
        ticketStatusMap.put("CLOSED", supportTicketRepository.countByStatus("CLOSED"));

        return new AdminDashboardStatsDto(
                totalStudents,
                totalConversations,
                totalMessages,
                openTickets,
                totalFaqs,
                unansweredCount,
                topIntents,
                ticketStatusMap
        );
    }

    @Transactional(readOnly = true)
    public List<UnansweredQueryDto> getUnansweredQueries() {
        return unansweredQueryRepository.findAll().stream().map(q -> new UnansweredQueryDto(
                q.getId(),
                q.getQueryText(),
                q.getPredictedIntent(),
                q.getModelScore(),
                q.getReviewStatus(),
                q.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void resolveUnansweredQuery(Long queryId, String resolution) {
        UnansweredQuery query = unansweredQueryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Unanswered query not found with ID: " + queryId));
        query.setReviewStatus(resolution != null ? resolution : "RESOLVED");
        unansweredQueryRepository.save(query);
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(u -> "ROLE_STUDENT".equals(u.getRole()))
                .map(u -> new UserProfileDto(
                        u.getId(),
                        u.getFullName(),
                        u.getStudentId(),
                        u.getEmail(),
                        u.getMobileNumber(),
                        u.getDepartment(),
                        u.getCourse(),
                        u.getSemester(),
                        u.getRole(),
                        u.getAccountStatus()
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatbotFeedback> getAllFeedback() {
        return chatbotFeedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public ModelInfoResponse getModelInfo() {
        return aiServiceClient.getModelInfo();
    }

    public Map<String, Object> getModelMetrics() {
        return aiServiceClient.getModelMetrics();
    }

    public Map<String, Object> getModelComparison() {
        return aiServiceClient.getModelComparison();
    }
}
