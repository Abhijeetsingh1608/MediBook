package com.medibook.payment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private String status;
}
