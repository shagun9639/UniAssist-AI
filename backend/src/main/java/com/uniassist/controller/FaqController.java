package com.uniassist.controller;

import com.uniassist.dto.FaqDto;
import com.uniassist.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@Tag(name = "Knowledge Base & FAQs", description = "Public university FAQs and knowledge search")
public class FaqController {

    private final FaqService faqService;

    public FaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    @Operation(summary = "Get all active university FAQs")
    public ResponseEntity<List<FaqDto>> getAllFaqs(@RequestParam(required = false) String category) {
        if (category != null && !category.trim().isEmpty()) {
            return ResponseEntity.ok(faqService.getFaqsByCategory(category.trim()));
        }
        return ResponseEntity.ok(faqService.getAllActiveFaqs());
    }

    @GetMapping("/search")
    @Operation(summary = "Search FAQs by keyword, question, or answer text")
    public ResponseEntity<List<FaqDto>> searchFaqs(@RequestParam(required = false, defaultValue = "") String query) {
        return ResponseEntity.ok(faqService.searchFaqs(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of a specific FAQ entry")
    public ResponseEntity<FaqDto> getFaqById(@PathVariable Long id) {
        return ResponseEntity.ok(faqService.getFaqById(id));
    }
}
