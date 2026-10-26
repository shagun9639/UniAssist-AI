package com.uniassist.service;

import com.uniassist.client.AiServiceClient;
import com.uniassist.dto.ChatDtos.*;
import com.uniassist.entity.*;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.exception.UnauthorizedAccessException;
import com.uniassist.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FaqRepository faqRepository;
    private final UnansweredQueryRepository unansweredQueryRepository;
    private final ChatbotFeedbackRepository chatbotFeedbackRepository;
    private final AiServiceClient aiServiceClient;

    public ChatbotService(ConversationRepository conversationRepository,
                          MessageRepository messageRepository,
                          UserRepository userRepository,
                          FaqRepository faqRepository,
                          UnansweredQueryRepository unansweredQueryRepository,
                          ChatbotFeedbackRepository chatbotFeedbackRepository,
                          AiServiceClient aiServiceClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.faqRepository = faqRepository;
        this.unansweredQueryRepository = unansweredQueryRepository;
        this.chatbotFeedbackRepository = chatbotFeedbackRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public ChatMessageResponse processMessage(Long userId, ChatMessageRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Conversation conversation;
        boolean isNewConversation = false;

        if (req.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(req.getConversationId(), userId)
                    .orElseThrow(() -> new UnauthorizedAccessException("Conversation not found or access denied."));
        } else {
            String title = req.getMessage().length() > 40
                    ? req.getMessage().substring(0, 37) + "..."
                    : req.getMessage();
            conversation = new Conversation(user, title);
            conversation = conversationRepository.save(conversation);
            isNewConversation = true;
        }

        // 1. Save student message
        Message studentMessage = new Message(conversation, "STUDENT", req.getMessage());
        messageRepository.save(studentMessage);

        // 2. Call Python AI/ML microservice
        Map<String, Object> aiResult = aiServiceClient.chat(req.getMessage(), conversation.getId(), userId);

        String answer;
        String intent;
        Double confidence;
        String source;
        Boolean fallback;
        Map<String, Object> entities = new HashMap<>();
        List<String> suggestedFollowups = new ArrayList<>();

        if (aiResult != null && !aiResult.isEmpty() && aiResult.containsKey("answer")) {
            answer = (String) aiResult.get("answer");
            intent = (String) aiResult.get("intent");
            confidence = aiResult.get("confidence") instanceof Number
                    ? ((Number) aiResult.get("confidence")).doubleValue() : 0.85;
            source = (String) aiResult.get("source");
            fallback = Boolean.TRUE.equals(aiResult.get("fallback"));

            if (aiResult.get("entities") instanceof Map) {
                entities = (Map<String, Object>) aiResult.get("entities");
            }
            if (aiResult.get("suggested_followups") instanceof List) {
                suggestedFollowups = (List<String>) aiResult.get("suggested_followups");
            }
        } else {
            // Local fallback when Python AI microservice is not running
            List<Faq> matchedFaqs = faqRepository.searchFaqs(req.getMessage());
            if (!matchedFaqs.isEmpty()) {
                Faq bestMatch = matchedFaqs.get(0);
                answer = bestMatch.getAnswer();
                intent = "faq_local_match";
                confidence = 0.85;
                source = "University Official FAQ #" + bestMatch.getId() + " (" + bestMatch.getCategory() + ")";
                fallback = false;
                suggestedFollowups = Arrays.asList(
                        "What is the attendance policy?",
                        "When are semester exams scheduled?",
                        "How do I pay my tuition fees online?"
                );
            } else {
                answer = "I apologize, but I could not find a verified answer for that question in the university handbook. "
                        + "Please try rephrasing your query, browse the FAQs or Academic sections, or submit an official support ticket.";
                intent = "unrecognized_query";
                confidence = 0.20;
                source = "UniAssist Fallback System";
                fallback = true;
                suggestedFollowups = Arrays.asList(
                        "What is the minimum attendance requirement?",
                        "How do I check my exam schedule?",
                        "Submit a support ticket"
                );
            }
        }

        // 3. Log fallback or low-confidence queries for administrator review
        if (Boolean.TRUE.equals(fallback)) {
            UnansweredQuery unanswered = new UnansweredQuery(req.getMessage(), intent, confidence);
            unansweredQueryRepository.save(unanswered);
        }

        // 4. Save assistant response turn
        Message assistantMessage = new Message(
                conversation,
                "ASSISTANT",
                answer,
                intent,
                confidence,
                source,
                fallback
        );
        Message savedAssistantMessage = messageRepository.save(assistantMessage);

        // Update conversation updated_at
        conversationRepository.save(conversation);

        return new ChatMessageResponse(
                conversation.getId(),
                savedAssistantMessage.getId(),
                answer,
                intent,
                confidence,
                source,
                fallback,
                entities,
                suggestedFollowups
        );
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(Long userId) {
        List<Conversation> convs = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return convs.stream().map(c -> new ConversationDto(
                c.getId(),
                c.getTitle(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getMessages().size()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversationMessages(Long userId, Long conversationId) {
        Conversation conv = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("Conversation not found or access denied."));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream().map(m -> new MessageDto(
                m.getId(),
                m.getSenderType(),
                m.getMessageText(),
                m.getPredictedIntent(),
                m.getModelScore(),
                m.getSourceReference(),
                m.getFallbackUsed(),
                m.getCreatedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation conv = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new UnauthorizedAccessException("Conversation not found or access denied."));
        conversationRepository.delete(conv);
    }

    @Transactional
    public void recordFeedback(Long userId, FeedbackRequestDto req) {
        User user = userRepository.findById(userId).orElse(null);
        Message msg = null;
        if (req.getMessageId() != null) {
            msg = messageRepository.findById(req.getMessageId()).orElse(null);
        }

        ChatbotFeedback feedback = new ChatbotFeedback(user, msg, req.getRating(), req.getComments());
        chatbotFeedbackRepository.save(feedback);

        if (msg != null) {
            aiServiceClient.sendFeedback(
                    msg.getMessageText(),
                    msg.getPredictedIntent() != null ? msg.getPredictedIntent() : "unknown",
                    req.getRating(),
                    req.getComments(),
                    msg.getId()
            );
        }
    }
}
