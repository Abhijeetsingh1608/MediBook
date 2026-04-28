package com.medibook.record.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for PatientProfileRequest.
 * It helps transfer only the required fields between layers.
 */
public class PatientProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String gender;
    private LocalDate dateOfBirth;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String allergies;
    private String profilePicUrl;
}
