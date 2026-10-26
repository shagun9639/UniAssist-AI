package com.uniassist.service;

import com.uniassist.dto.AcademicDtos.*;
import com.uniassist.entity.AcademicCourse;
import com.uniassist.entity.AcademicEvent;
import com.uniassist.entity.Announcement;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.repository.AcademicCourseRepository;
import com.uniassist.repository.AcademicEventRepository;
import com.uniassist.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicService {

    private final AcademicCourseRepository courseRepository;
    private final AcademicEventRepository eventRepository;
    private final AnnouncementRepository announcementRepository;

    public AcademicService(AcademicCourseRepository courseRepository,
                           AcademicEventRepository eventRepository,
                           AnnouncementRepository announcementRepository) {
        this.courseRepository = courseRepository;
        this.eventRepository = eventRepository;
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream().map(c -> new CourseDto(
                c.getId(),
                c.getCourseCode(),
                c.getCourseName(),
                c.getDepartment(),
                c.getCredits(),
                c.getDurationSemesters(),
                c.getDescription()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventDto> getUpcomingEvents() {
        return eventRepository.findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate.now().minusDays(30))
                .stream().map(e -> new EventDto(
                        e.getId(),
                        e.getTitle(),
                        e.getEventType(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getDescription(),
                        e.getSemester()
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> getActiveAnnouncements() {
        return announcementRepository.findByActiveTrueOrderByPublishedAtDesc()
                .stream().map(a -> new AnnouncementDto(
                        a.getId(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getCategory(),
                        a.getPublishedAt(),
                        a.getExpiryDate(),
                        a.getActive()
                )).collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto createAnnouncement(AnnouncementDto dto) {
        Announcement a = new Announcement(
                dto.getTitle(),
                dto.getDescription(),
                dto.getCategory(),
                dto.getExpiryDate()
        );
        Announcement saved = announcementRepository.save(a);
        return new AnnouncementDto(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getCategory(),
                saved.getPublishedAt(),
                saved.getExpiryDate(),
                saved.getActive()
        );
    }

    @Transactional
    public AnnouncementDto updateAnnouncement(Long id, AnnouncementDto dto) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with ID: " + id));

        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setCategory(dto.getCategory());
        a.setExpiryDate(dto.getExpiryDate());
        if (dto.getActive() != null) {
            a.setActive(dto.getActive());
        }

        Announcement saved = announcementRepository.save(a);
        return new AnnouncementDto(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getCategory(),
                saved.getPublishedAt(),
                saved.getExpiryDate(),
                saved.getActive()
        );
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found with ID: " + id);
        }
        announcementRepository.deleteById(id);
    }
}
