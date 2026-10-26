package com.uniassist.service;

import com.uniassist.dto.FaqDto;
import com.uniassist.entity.Faq;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public List<FaqDto> getAllActiveFaqs() {
        return faqRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqDto> searchFaqs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveFaqs();
        }
        return faqRepository.searchFaqs(query.trim()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqDto> getFaqsByCategory(String category) {
        return faqRepository.findByCategoryAndActiveTrue(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FaqDto getFaqById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with ID: " + id));
        return toDto(faq);
    }

    @Transactional
    public FaqDto createFaq(FaqDto dto) {
        Faq faq = new Faq(
                dto.getQuestion(),
                dto.getAnswer(),
                dto.getCategory(),
                dto.getKeywords(),
                dto.getSourceUrl(),
                dto.getActive() != null ? dto.getActive() : true
        );
        return toDto(faqRepository.save(faq));
    }

    @Transactional
    public FaqDto updateFaq(Long id, FaqDto dto) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ not found with ID: " + id));

        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());
        faq.setCategory(dto.getCategory());
        faq.setKeywords(dto.getKeywords());
        faq.setSourceUrl(dto.getSourceUrl());
        if (dto.getActive() != null) {
            faq.setActive(dto.getActive());
        }

        return toDto(faqRepository.save(faq));
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new ResourceNotFoundException("FAQ not found with ID: " + id);
        }
        faqRepository.deleteById(id);
    }

    private FaqDto toDto(Faq f) {
        return new FaqDto(
                f.getId(),
                f.getQuestion(),
                f.getAnswer(),
                f.getCategory(),
                f.getKeywords(),
                f.getSourceUrl(),
                f.getActive(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
