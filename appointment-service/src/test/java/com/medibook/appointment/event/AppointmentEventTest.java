package com.medibook.appointment.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AppointmentEventTest {

    @Test
    void testBookedEvent() {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder()
                .appointmentId(1L)
                .patientUserId(10L)
                .providerId(3L)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10,0))
                .endTime(LocalTime.of(10,30))
                .reason("Test")
                .build();
        assertThat(event.getAppointmentId()).isEqualTo(1L);
        assertThat(event.getReason()).isEqualTo("Test");
    }

    @Test
    void testCancelledEvent() {
        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                .appointmentId(1L)
                .cancelledByRole("PATIENT")
                .build();
        assertThat(event.getAppointmentId()).isEqualTo(1L);
        assertThat(event.getCancelledByRole()).isEqualTo("PATIENT");
    }
}
