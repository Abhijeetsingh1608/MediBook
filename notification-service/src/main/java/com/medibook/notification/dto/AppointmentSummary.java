package com.medibook.notification.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO carries appointment details from appointment service
 * to notification service. It helps us send reminder mails
 * without exposing full database logic here.
 */
public class AppointmentSummary {

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
