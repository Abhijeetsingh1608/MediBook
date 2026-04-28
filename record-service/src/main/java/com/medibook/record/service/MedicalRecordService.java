package com.medibook.record.service;

import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.entity.MedicalRecord;
import java.util.List;

/*
 * This is the service interface for MedicalRecordService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface MedicalRecordService {
    MedicalRecord createMedicalRecord(MedicalRecordRequest request, String role);

    MedicalRecord getMedicalRecordById(Long recordId, Long loggedInUserId, String role);

    List<MedicalRecord> getAllMedicalRecords();

    List<MedicalRecord> getRecordsByPatient(Long patientUserId, Long loggedInUserId, String role);

    List<MedicalRecord> getRecordsByProvider(Long providerId);

    MedicalRecord updateMedicalRecord(Long recordId, MedicalRecordRequest request, String role);

    void deleteMedicalRecord(Long recordId, String role);
}
