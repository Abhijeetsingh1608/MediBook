package com.medibook.schedule.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class DtoTest {

    @Test
    void apiMessage_builder() {
        ApiMessage msg = ApiMessage.builder().message("test").build();
        assertThat(msg.getMessage()).isEqualTo("test");
    }

    @Test
    void slotRequest_builder() {
        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(10, 0);
        
        SlotRequest request = SlotRequest.builder()
                .providerId(1L)
                .slotDate(date)
                .startTime(start)
                .build();
        
        assertThat(request.getProviderId()).isEqualTo(1L);
        assertThat(request.getSlotDate()).isEqualTo(date);
        assertThat(request.getStartTime()).isEqualTo(start);
    }
}
