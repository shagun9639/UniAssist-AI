package com.uniassist.repository;

import com.uniassist.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
