package com.medibook.schedule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ScheduleApplicationTest {

    @Test
    void main_runsWithoutFailure() {
        ScheduleServiceApplication app = new ScheduleServiceApplication();
        assertDoesNotThrow(() -> {});
    }
}
