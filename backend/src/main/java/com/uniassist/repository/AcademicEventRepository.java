package com.uniassist.repository;

import com.uniassist.entity.AcademicEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AcademicEventRepository extends JpaRepository<AcademicEvent, Long> {
    List<AcademicEvent> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);
    List<AcademicEvent> findByEventType(String eventType);
}
