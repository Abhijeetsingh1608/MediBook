package com.medibook.appointment.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AppointmentDTOTest {

    @Test
    void testAppointmentRequest() {
        AppointmentRequest request = new AppointmentRequest(1L, 7L, "Reason");
        assertThat(request.getProviderId()).isEqualTo(1L);
        assertThat(request.getSlotId()).isEqualTo(7L);
        assertThat(request.getReason()).isEqualTo("Reason");
        
        request = new AppointmentRequest();
        request.setProviderId(2L);
        assertThat(request.getProviderId()).isEqualTo(2L);
    }

    @Test
    void testScheduleSlotResponse() {
        ScheduleSlotResponse response = new ScheduleSlotResponse();
        response.setSlotId(1L);
        response.setProviderId(3L);
        response.setSlotDate(LocalDate.now());
        response.setStartTime(LocalTime.of(10,0));
        response.setEndTime(LocalTime.of(10,30));
        response.setStatus("AVAILABLE");
        
        assertThat(response.getSlotId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void testApiMessage() {
        ApiMessage message = new ApiMessage("Test");
        assertThat(message.getMessage()).isEqualTo("Test");
        message.setMessage("New");
        assertThat(message.getMessage()).isEqualTo("New");
    }
}
