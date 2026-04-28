package com.medibook.record.service;

import com.medibook.record.dto.PatientProfileRequest;
import com.medibook.record.entity.PatientProfile;
import com.medibook.record.repository.PatientProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for PatientProfileServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class PatientProfileServiceImpl implements PatientProfileService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final PatientProfileRepository patientProfileRepository;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public PatientProfile createProfile(PatientProfileRequest request, Long userId, String role) {
        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only patient users can create patient profile");
        }

        if (patientProfileRepository.existsByUserId(userId)) {
            throw new RuntimeException("Patient profile already exists");
        }

        PatientProfile profile = PatientProfile.builder()
                .userId(userId)
                .fullName(request.getFullName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup())
                .phone(request.getPhone())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .allergies(request.getAllergies())
                .profilePicUrl(request.getProfilePicUrl())
                .build();

        return patientProfileRepository.save(profile);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public PatientProfile getProfileByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<PatientProfile> getAllProfiles() {
        return patientProfileRepository.findAll();
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public PatientProfile updateProfile(
            Long patientUserId,
            PatientProfileRequest request,
            Long loggedInUserId,
            String role) {

        if (!"ADMIN".equalsIgnoreCase(role) && !patientUserId.equals(loggedInUserId)) {
            throw new RuntimeException("You can update only your own patient profile");
        }

        PatientProfile profile = getProfileByUserId(patientUserId);
        profile.setFullName(request.getFullName());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setEmergencyContactName(request.getEmergencyContactName());
        profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        profile.setAllergies(request.getAllergies());
        profile.setProfilePicUrl(request.getProfilePicUrl());
        return patientProfileRepository.save(profile);
    }
}
