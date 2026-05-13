package com.medibook.appointment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class AppointmentApplicationTest {

    @Test
    void main_runsWithoutFailure() {
        AppointmentServiceApplication app = new AppointmentServiceApplication();
        assertDoesNotThrow(() -> {});
    }
}
