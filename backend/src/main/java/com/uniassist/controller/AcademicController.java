package com.uniassist.controller;

import com.uniassist.dto.AcademicDtos.*;
import com.uniassist.service.AcademicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Academic & Campus Info", description = "University courses, academic calendar, notices, and announcements")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    @GetMapping("/academic/courses")
    @Operation(summary = "List all academic degree programs and courses")
    public ResponseEntity<List<CourseDto>> getCourses() {
        return ResponseEntity.ok(academicService.getAllCourses());
    }

    @GetMapping("/academic/calendar")
    @Operation(summary = "Get upcoming academic calendar milestones, exams, and holidays")
    public ResponseEntity<List<EventDto>> getAcademicCalendar() {
        return ResponseEntity.ok(academicService.getUpcomingEvents());
    }

    @GetMapping("/announcements")
    @Operation(summary = "Get active campus notices and announcements")
    public ResponseEntity<List<AnnouncementDto>> getAnnouncements() {
        return ResponseEntity.ok(academicService.getActiveAnnouncements());
    }
}
