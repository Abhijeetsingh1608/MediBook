package com.medibook.schedule.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AppointmentSlotEntityTest {

    @Test
    void entity_builderAndGetters() {
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        
        AppointmentSlot slot = AppointmentSlot.builder()
                .slotId(1L)
                .providerId(2L)
                .slotDate(date)
                .startTime(start)
                .endTime(end)
                .status(SlotStatus.AVAILABLE)
                .build();
        
        assertThat(slot.getSlotId()).isEqualTo(1L);
        assertThat(slot.getProviderId()).isEqualTo(2L);
        assertThat(slot.getSlotDate()).isEqualTo(date);
        assertThat(slot.getStartTime()).isEqualTo(start);
        assertThat(slot.getEndTime()).isEqualTo(end);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }
}
