package com.medibook.record.repository;

import com.medibook.record.entity.MedicalRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for MedicalRecordRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    boolean existsByAppointmentId(Long appointmentId);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    List<MedicalRecord> findByPatientUserId(Long patientUserId);

    List<MedicalRecord> findByProviderId(Long providerId);
}
