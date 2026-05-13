package com.medibook.schedule.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlotStatusTest {

    @Test
    void enumValues() {
        assertThat(SlotStatus.valueOf("AVAILABLE")).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(SlotStatus.valueOf("BOOKED")).isEqualTo(SlotStatus.BOOKED);
        assertThat(SlotStatus.valueOf("CANCELLED")).isEqualTo(SlotStatus.CANCELLED);
    }
}
