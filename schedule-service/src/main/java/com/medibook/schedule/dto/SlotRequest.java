package com.medibook.schedule.dto;

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
 * This DTO is used to carry request/response data for SlotRequest.
 * It helps transfer only the required fields between layers.
 */
public class SlotRequest {
    private Long providerId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
