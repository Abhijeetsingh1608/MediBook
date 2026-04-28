package com.medibook.record.controller;

import com.medibook.record.dto.PatientProfileRequest;
import com.medibook.record.entity.PatientProfile;
import com.medibook.record.service.PatientProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records/patients")
@RequiredArgsConstructor
/*
 * This controller handles API requests for PatientProfileController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class PatientProfileController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final PatientProfileService patientProfileService;

    @PostMapping
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public PatientProfile createProfile(
            @Valid @RequestBody PatientProfileRequest request,
            HttpServletRequest httpRequest) {
        return patientProfileService.createProfile(
                request,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @GetMapping("/me")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public PatientProfile getMyProfile(HttpServletRequest httpRequest) {
        return patientProfileService.getProfileByUserId(getUserId(httpRequest));
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<PatientProfile> getAllProfiles() {
        return patientProfileService.getAllProfiles();
    }

    @GetMapping("/{patientUserId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public PatientProfile getProfileByUserId(@PathVariable Long patientUserId) {
        return patientProfileService.getProfileByUserId(patientUserId);
    }

    @PutMapping("/{patientUserId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public PatientProfile updateProfile(
            @PathVariable Long patientUserId,
            @Valid @RequestBody PatientProfileRequest request,
            HttpServletRequest httpRequest) {
        return patientProfileService.updateProfile(
                patientUserId,
                request,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private Long getUserId(HttpServletRequest request) {
        return Long.valueOf(request.getHeader("X-User-Id"));
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private String getRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }
}
