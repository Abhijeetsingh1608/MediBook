package com.medibook.review.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.medibook.review.dto.AppointmentResponse;
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
        ReflectionTestUtils.setField(appointmentClient, "internalSecret", "test-secret");
    }

    @Test
    void getAppointmentById_success() {
        AppointmentResponse response = new AppointmentResponse();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AppointmentResponse.class),
                any(java.util.Map.class)))
                .thenReturn(ResponseEntity.ok(response));

        AppointmentResponse result = appointmentClient.getAppointmentById(1L);
        assertThat(result).isEqualTo(response);
    }
}
