package com.uniassist.repository;

import com.uniassist.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByActiveTrueOrderByPublishedAtDesc();
    List<Announcement> findByCategoryAndActiveTrue(String category);
}
