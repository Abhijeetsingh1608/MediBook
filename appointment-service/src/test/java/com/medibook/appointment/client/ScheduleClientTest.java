package com.medibook.appointment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.appointment.dto.ScheduleSlotResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ScheduleClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ScheduleClient scheduleClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduleClient, "internalSecret", "test-secret");
    }

    @Test
    void getSlotById_success() {
        ScheduleSlotResponse response = new ScheduleSlotResponse();
        when(restTemplate.exchange(
                eq("http://SCHEDULE-SERVICE/api/v1/slots/{slotId}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(ScheduleSlotResponse.class),
                any(java.util.Map.class)))
                .thenReturn(ResponseEntity.of(java.util.Optional.of(response)));

        ScheduleSlotResponse result = scheduleClient.getSlotById(1L);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void updateSlotStatus_success() {
        when(restTemplate.exchange(
                eq("http://SCHEDULE-SERVICE/api/v1/slots/{slotId}/status?status={status}"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class),
                any(java.util.Map.class)))
                .thenReturn(ResponseEntity.ok().build());

        scheduleClient.updateSlotStatus(1L, "BOOKED");
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class), any(java.util.Map.class));
    }
}
