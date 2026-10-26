package com.uniassist;

import com.uniassist.dto.ChatDtos.ChatMessageRequest;
import com.uniassist.dto.ChatDtos.ChatMessageResponse;
import com.uniassist.entity.User;
import com.uniassist.repository.UserRepository;
import com.uniassist.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ChatbotServiceTest {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testProcessMessageAndFallback() {
        User student = userRepository.findByEmail("student@uniassist.edu").orElseThrow();

        ChatMessageRequest req = new ChatMessageRequest();
        req.setMessage("What is the mandatory attendance requirement?");

        ChatMessageResponse response = chatbotService.processMessage(student.getId(), req);

        assertNotNull(response);
        assertNotNull(response.getConversationId());
        assertNotNull(response.getReply());
        assertNotNull(response.getSource());
        assertFalse(response.getReply().isEmpty());

        // Test conversations retrieval
        var conversations = chatbotService.getUserConversations(student.getId());
        assertFalse(conversations.isEmpty());
    }
}
