package com.uniassist;

import com.uniassist.dto.FaqDto;
import com.uniassist.service.FaqService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FaqServiceTest {

    @Autowired
    private FaqService faqService;

    @Test
    public void testFaqRetrievalAndSearch() {
        List<FaqDto> faqs = faqService.getAllActiveFaqs();
        assertFalse(faqs.isEmpty());
        assertTrue(faqs.size() >= 20);

        List<FaqDto> searchResults = faqService.searchFaqs("attendance");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(f -> f.getQuestion().toLowerCase().contains("attendance")));
    }
}
