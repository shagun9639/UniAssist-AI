package com.uniassist.repository;

import com.uniassist.entity.ChatbotFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotFeedbackRepository extends JpaRepository<ChatbotFeedback, Long> {
    List<ChatbotFeedback> findByReviewStatusOrderByCreatedAtDesc(String reviewStatus);
    long countByRating(String rating);
    List<ChatbotFeedback> findAllByOrderByCreatedAtDesc();
}
