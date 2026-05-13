package com.medibook.appointment.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AppointmentStatusTest {

    @Test
    void testEnumValues() {
        assertThat(AppointmentStatus.valueOf("BOOKED")).isEqualTo(AppointmentStatus.BOOKED);
        assertThat(AppointmentStatus.valueOf("CANCELLED")).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(AppointmentStatus.valueOf("COMPLETED")).isEqualTo(AppointmentStatus.COMPLETED);
    }
}
