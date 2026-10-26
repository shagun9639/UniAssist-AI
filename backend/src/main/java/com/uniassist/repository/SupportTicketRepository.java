package com.uniassist.repository;

import com.uniassist.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<SupportTicket> findByIdAndUserId(Long id, Long userId);
    List<SupportTicket> findByStatus(String status);
    long countByStatus(String status);
    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
