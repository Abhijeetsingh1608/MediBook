package com.medibook.record.controller;

import com.medibook.record.dto.ApiMessage;
import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.entity.MedicalRecord;
import com.medibook.record.service.MedicalRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records/medical")
@RequiredArgsConstructor
/*
 * This controller handles API requests for MedicalRecordController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class MedicalRecordController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final MedicalRecordService medicalRecordService;

    @PostMapping
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public MedicalRecord createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request,
            HttpServletRequest httpRequest) {
        return medicalRecordService.createMedicalRecord(request, getRole(httpRequest));
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordService.getAllMedicalRecords();
    }

    @GetMapping("/{recordId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public MedicalRecord getMedicalRecordById(
            @PathVariable Long recordId,
            HttpServletRequest httpRequest) {
        return medicalRecordService.getMedicalRecordById(
                recordId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @GetMapping("/patient/{patientUserId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<MedicalRecord> getRecordsByPatient(
            @PathVariable Long patientUserId,
            HttpServletRequest httpRequest) {
        return medicalRecordService.getRecordsByPatient(
                patientUserId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @GetMapping("/provider/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<MedicalRecord> getRecordsByProvider(@PathVariable Long providerId) {
        return medicalRecordService.getRecordsByProvider(providerId);
    }

    @PutMapping("/{recordId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public MedicalRecord updateMedicalRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody MedicalRecordRequest request,
            HttpServletRequest httpRequest) {
        return medicalRecordService.updateMedicalRecord(recordId, request, getRole(httpRequest));
    }

    @DeleteMapping("/{recordId}")
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public ApiMessage deleteMedicalRecord(
            @PathVariable Long recordId,
            HttpServletRequest httpRequest) {
        medicalRecordService.deleteMedicalRecord(recordId, getRole(httpRequest));
        return ApiMessage.builder()
                .message("Medical record deleted successfully")
                .build();
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
