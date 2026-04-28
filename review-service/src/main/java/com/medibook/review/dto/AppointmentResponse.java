package com.medibook.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * This DTO is used to carry request/response data for AppointmentResponse.
 * It helps transfer only the required fields between layers.
 */
public class AppointmentResponse {
    private Long appointmentId;
    private Long patientUserId;
    private Long providerId;
    private Long slotId;
    private String status;
}
