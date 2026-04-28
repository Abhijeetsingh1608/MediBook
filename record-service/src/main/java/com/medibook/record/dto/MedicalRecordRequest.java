package com.medibook.record.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for MedicalRecordRequest.
 * It helps transfer only the required fields between layers.
 */
public class MedicalRecordRequest {

    @NotNull(message = "Appointment id is required")
    private Long appointmentId;

    private String symptoms;
    private String diagnosis;
    private String doctorNotes;
    private LocalDate followUpDate;
    private String reportFileUrl;

    @Valid
    private List<PrescriptionItemRequest> prescriptions;
}
