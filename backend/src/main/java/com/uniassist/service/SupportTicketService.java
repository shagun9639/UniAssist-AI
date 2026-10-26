package com.uniassist.service;

import com.uniassist.dto.TicketDtos.*;
import com.uniassist.entity.SupportTicket;
import com.uniassist.entity.TicketResponse;
import com.uniassist.entity.User;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.exception.UnauthorizedAccessException;
import com.uniassist.repository.SupportTicketRepository;
import com.uniassist.repository.TicketResponseRepository;
import com.uniassist.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketResponseRepository responseRepository;
    private final UserRepository userRepository;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                                TicketResponseRepository responseRepository,
                                UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.responseRepository = responseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SupportTicketDto createTicket(Long userId, CreateTicketRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        SupportTicket ticket = new SupportTicket(
                user,
                req.getCategory(),
                req.getSubject(),
                req.getDescription(),
                req.getPriority()
        );

        return toDto(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDto> getUserTickets(Long userId) {
        return ticketRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream().map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupportTicketDto getTicketById(Long userId, Long ticketId, boolean isAdmin) {
        SupportTicket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));
        } else {
            ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
                    .orElseThrow(() -> new UnauthorizedAccessException("Ticket not found or access denied."));
        }
        return toDto(ticket);
    }

    @Transactional
    public TicketResponseDto addResponse(Long userId, Long ticketId, TicketResponseRequest req, boolean isAdmin) {
        SupportTicket ticket;
        if (isAdmin) {
            ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        } else {
            ticket = ticketRepository.findByIdAndUserId(ticketId, userId)
                    .orElseThrow(() -> new UnauthorizedAccessException("Ticket not found or access denied."));
        }

        User responder = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        TicketResponse response = new TicketResponse(ticket, responder, req.getResponseText());
        TicketResponse saved = responseRepository.save(response);

        // Update ticket status
        if (isAdmin && "OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
            ticketRepository.save(ticket);
        }

        return new TicketResponseDto(
                saved.getId(),
                responder.getId(),
                responder.getFullName(),
                saved.getResponseText(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<SupportTicketDto> getAllTicketsAdmin(String status) {
        List<SupportTicket> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            list = ticketRepository.findByStatus(status.toUpperCase());
        } else {
            list = ticketRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SupportTicketDto updateTicketStatus(Long ticketId, String status, String assignedTo) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (status != null && !status.trim().isEmpty()) {
            ticket.setStatus(status.toUpperCase());
        }
        if (assignedTo != null) {
            ticket.setAssignedTo(assignedTo);
        }

        return toDto(ticketRepository.save(ticket));
    }

    private SupportTicketDto toDto(SupportTicket t) {
        List<TicketResponseDto> responses = t.getResponses().stream().map(r -> new TicketResponseDto(
                r.getId(),
                r.getResponder().getId(),
                r.getResponder().getFullName(),
                r.getResponseText(),
                r.getCreatedAt()
        )).collect(Collectors.toList());

        return new SupportTicketDto(
                t.getId(),
                t.getUser().getId(),
                t.getUser().getFullName(),
                t.getUser().getEmail(),
                t.getCategory(),
                t.getSubject(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getAssignedTo(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                responses
        );
    }
}
