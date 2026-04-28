package com.medibook.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * This DTO is used to carry request/response data for ScheduleSlotResponse.
 * It helps transfer only the required fields between layers.
 */
public class ScheduleSlotResponse {
    private Long slotId;
    private Long providerId;
    private Long createdByUserId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
}
