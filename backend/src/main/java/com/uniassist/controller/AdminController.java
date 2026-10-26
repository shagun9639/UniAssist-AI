package com.uniassist.controller;

import com.uniassist.dto.AcademicDtos.AnnouncementDto;
import com.uniassist.dto.AdminDtos.*;
import com.uniassist.dto.AuthDtos.UserProfileDto;
import com.uniassist.dto.FaqDto;
import com.uniassist.dto.ModelInfoResponse;
import com.uniassist.dto.TicketDtos.*;
import com.uniassist.security.UserPrincipal;
import com.uniassist.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Admin dashboard, knowledge base management, ticket handling, and ML analytics")
public class AdminController {

    private final AdminService adminService;
    private final FaqService faqService;
    private final AcademicService academicService;
    private final SupportTicketService ticketService;

    public AdminController(AdminService adminService,
                           FaqService faqService,
                           AcademicService academicService,
                           SupportTicketService ticketService) {
        this.adminService = adminService;
        this.faqService = faqService;
        this.academicService = academicService;
        this.ticketService = ticketService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated statistics for administration dashboard")
    public ResponseEntity<AdminDashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get detailed analytics metrics for queries and feedback")
    public ResponseEntity<AdminDashboardStatsDto> getAnalytics() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered university students")
    public ResponseEntity<List<UserProfileDto>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/unanswered-queries")
    @Operation(summary = "List queries flagged as low-confidence or unanswered for human review")
    public ResponseEntity<List<UnansweredQueryDto>> getUnansweredQueries() {
        return ResponseEntity.ok(adminService.getUnansweredQueries());
    }

    @PutMapping("/unanswered-queries/{id}")
    @Operation(summary = "Update resolution status of an unanswered query")
    public ResponseEntity<Map<String, String>> resolveUnansweredQuery(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String resolution = payload.getOrDefault("status", "RESOLVED");
        adminService.resolveUnansweredQuery(id, resolution);
        return ResponseEntity.ok(Map.of("message", "Query status updated successfully."));
    }

    // Support Ticket Administration
    @GetMapping("/tickets")
    @Operation(summary = "List support tickets with optional status filter")
    public ResponseEntity<List<SupportTicketDto>> getAllTickets(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ticketService.getAllTicketsAdmin(status));
    }

    @PutMapping("/tickets/{id}/status")
    @Operation(summary = "Update support ticket status and assignee")
    public ResponseEntity<SupportTicketDto> updateTicketStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String assignedTo = payload.get("assignedTo");
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status, assignedTo));
    }

    @PostMapping("/tickets/{id}/responses")
    @Operation(summary = "Post official staff response to a student ticket")
    public ResponseEntity<TicketResponseDto> addStaffResponse(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody TicketResponseRequest req) {
        return ResponseEntity.ok(ticketService.addResponse(currentUser.getId(), id, req, true));
    }

    // FAQ Knowledge Base Management
    @PostMapping("/faqs")
    @Operation(summary = "Create a new verified FAQ entry")
    public ResponseEntity<FaqDto> createFaq(@Valid @RequestBody FaqDto dto) {
        return new ResponseEntity<>(faqService.createFaq(dto), HttpStatus.CREATED);
    }

    @PutMapping("/faqs/{id}")
    @Operation(summary = "Update an existing FAQ entry")
    public ResponseEntity<FaqDto> updateFaq(@PathVariable Long id, @Valid @RequestBody FaqDto dto) {
        return ResponseEntity.ok(faqService.updateFaq(id, dto));
    }

    @DeleteMapping("/faqs/{id}")
    @Operation(summary = "Delete an FAQ entry")
    public ResponseEntity<Map<String, String>> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.ok(Map.of("message", "FAQ deleted successfully."));
    }

    // Announcements Management
    @PostMapping("/announcements")
    @Operation(summary = "Publish a new campus announcement")
    public ResponseEntity<AnnouncementDto> createAnnouncement(@RequestBody AnnouncementDto dto) {
        return new ResponseEntity<>(academicService.createAnnouncement(dto), HttpStatus.CREATED);
    }

    @PutMapping("/announcements/{id}")
    @Operation(summary = "Update an announcement")
    public ResponseEntity<AnnouncementDto> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementDto dto) {
        return ResponseEntity.ok(academicService.updateAnnouncement(id, dto));
    }

    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "Delete an announcement")
    public ResponseEntity<Map<String, String>> deleteAnnouncement(@PathVariable Long id) {
        academicService.deleteAnnouncement(id);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted successfully."));
    }

    // AI/ML Model Analytics
    @GetMapping("/model-info")
    @Operation(summary = "Get active machine learning model metadata and training parameters")
    public ResponseEntity<ModelInfoResponse> getModelInfo() {
        return ResponseEntity.ok(adminService.getModelInfo());
    }

    @GetMapping("/model-metrics")
    @Operation(summary = "Get detailed test classification metrics and per-intent breakdown")
    public ResponseEntity<Map<String, Object>> getModelMetrics() {
        return ResponseEntity.ok(adminService.getModelMetrics());
    }

    @GetMapping("/model-comparison")
    @Operation(summary = "Get comparative metrics between Logistic Regression and Calibrated Linear SVC")
    public ResponseEntity<Map<String, Object>> getModelComparison() {
        return ResponseEntity.ok(adminService.getModelComparison());
    }
}
