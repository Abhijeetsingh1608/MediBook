package com.medibook.record.repository;

import com.medibook.record.entity.PatientProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for PatientProfileRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    boolean existsByUserId(Long userId);

    Optional<PatientProfile> findByUserId(Long userId);
}
