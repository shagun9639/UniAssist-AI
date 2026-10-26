package com.uniassist.controller;

import com.uniassist.dto.TicketDtos.*;
import com.uniassist.security.UserPrincipal;
import com.uniassist.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Support Tickets", description = "Student support grievances, ticket submissions, and status tracking")
public class SupportTicketController {

    private final SupportTicketService ticketService;

    public SupportTicketController(SupportTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @Operation(summary = "Submit a new student support request")
    public ResponseEntity<SupportTicketDto> createTicket(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateTicketRequest req) {
        return new ResponseEntity<>(ticketService.createTicket(currentUser.getId(), req), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "View all tickets submitted by the authenticated student")
    public ResponseEntity<List<SupportTicketDto>> getMyTickets(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(ticketService.getUserTickets(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details and staff responses for a specific ticket")
    public ResponseEntity<SupportTicketDto> getTicketById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ticketService.getTicketById(currentUser.getId(), id, isAdmin));
    }

    @PostMapping("/{id}/responses")
    @Operation(summary = "Add a follow-up reply to an existing support ticket")
    public ResponseEntity<TicketResponseDto> addResponse(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody TicketResponseRequest req) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ticketService.addResponse(currentUser.getId(), id, req, isAdmin));
    }
}
