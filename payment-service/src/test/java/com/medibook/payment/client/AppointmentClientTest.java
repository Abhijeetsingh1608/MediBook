package com.medibook.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.medibook.payment.dto.AppointmentResponse;
import java.util.Map;
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
class AppointmentClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AppointmentClient appointmentClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appointmentClient, "internalSecret", "secret");
    }

    @Test
    void getAppointmentById_success() {
        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(1L);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AppointmentResponse.class),
                anyMap()
        )).thenReturn(ResponseEntity.of(java.util.Optional.of(response)));

        AppointmentResponse result = appointmentClient.getAppointmentById(1L);

        assertThat(result.getAppointmentId()).isEqualTo(1L);
    }

    @Test
    void activatePaidAppointment_success() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class),
                anyMap()
        )).thenReturn(ResponseEntity.ok().build());

        appointmentClient.activatePaidAppointment(1L);
        // Success if no exception
    }
}
