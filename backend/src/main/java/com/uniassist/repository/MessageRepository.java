package com.uniassist.repository;

import com.uniassist.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    @Query("SELECT m.predictedIntent, COUNT(m) FROM Message m WHERE m.predictedIntent IS NOT NULL GROUP BY m.predictedIntent ORDER BY COUNT(m) DESC")
    List<Object[]> findTopIntents();

    long countByFallbackUsed(Boolean fallbackUsed);
}
