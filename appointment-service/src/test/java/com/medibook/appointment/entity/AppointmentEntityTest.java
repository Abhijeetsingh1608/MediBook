package com.medibook.appointment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AppointmentEntityTest {

    @Test
    void testAppointmentGettersAndSetters() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(1L);
        appointment.setPatientUserId(10L);
        appointment.setProviderId(3L);
        appointment.setSlotId(7L);
        appointment.setAppointmentDate(LocalDate.now());
        appointment.setStartTime(LocalTime.of(10,0));
        appointment.setEndTime(LocalTime.of(10,30));
        appointment.setReason("Test");
        appointment.setStatus(AppointmentStatus.BOOKED);

        assertThat(appointment.getAppointmentId()).isEqualTo(1L);
        assertThat(appointment.getPatientUserId()).isEqualTo(10L);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
    }

    @Test
    void testBuilder() {
        Appointment appointment = Appointment.builder()
                .appointmentId(2L)
                .status(AppointmentStatus.COMPLETED)
                .build();
        assertThat(appointment.getAppointmentId()).isEqualTo(2L);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }
}
