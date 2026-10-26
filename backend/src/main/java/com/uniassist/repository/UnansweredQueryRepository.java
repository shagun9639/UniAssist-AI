package com.uniassist.repository;

import com.uniassist.entity.UnansweredQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnansweredQueryRepository extends JpaRepository<UnansweredQuery, Long> {
    List<UnansweredQuery> findByReviewStatusOrderByCreatedAtDesc(String reviewStatus);
    long countByReviewStatus(String reviewStatus);
}
