package com.uniassist.repository;

import com.uniassist.entity.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelVersionRepository extends JpaRepository<ModelVersion, Long> {
    Optional<ModelVersion> findTopByOrderByCreatedAtDesc();
}
