package com.uniassist.repository;

import com.uniassist.entity.AcademicCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicCourseRepository extends JpaRepository<AcademicCourse, Long> {
    Optional<AcademicCourse> findByCourseCode(String courseCode);
    List<AcademicCourse> findByDepartment(String department);
}
