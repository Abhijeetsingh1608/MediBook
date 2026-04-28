package com.medibook.record.service;

import com.medibook.record.dto.PatientProfileRequest;
import com.medibook.record.entity.PatientProfile;
import java.util.List;

/*
 * This is the service interface for PatientProfileService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface PatientProfileService {
    PatientProfile createProfile(PatientProfileRequest request, Long userId, String role);

    PatientProfile getProfileByUserId(Long userId);

    List<PatientProfile> getAllProfiles();

    PatientProfile updateProfile(Long patientUserId, PatientProfileRequest request, Long loggedInUserId, String role);
}
