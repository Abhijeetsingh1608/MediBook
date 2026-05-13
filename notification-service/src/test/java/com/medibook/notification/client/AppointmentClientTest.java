package com.medibook.notification.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.medibook.notification.dto.AppointmentSummary;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class AppointmentClientTest {

    private RestTemplate restTemplate;
    private AppointmentClient appointmentClient;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        appointmentClient = new AppointmentClient(restTemplate);
        ReflectionTestUtils.setField(appointmentClient, "internalSecret", "secret");
    }

    @Test
    void getBookedAppointmentsForDate_returnsEmptyListWhenBodyMissing() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        when(restTemplate.exchange(
                eq("http://APPOINTMENT-SERVICE/api/v1/appointments/internal/booked-by-date?date={date}"),
                eq(HttpMethod.GET),
                any(),
                eq(AppointmentSummary[].class),
                eq(java.util.Map.of("date", date))))
                .thenReturn(ResponseEntity.ok(null));

        List<AppointmentSummary> response = appointmentClient.getBookedAppointmentsForDate(date);

        assertThat(response).isEmpty();
    }

    @Test
    void getBookedAppointmentsForDate_returnsResponseBody() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        AppointmentSummary[] appointments = {
                AppointmentSummary.builder().appointmentId(11L).build()
        };
        when(restTemplate.exchange(
                eq("http://APPOINTMENT-SERVICE/api/v1/appointments/internal/booked-by-date?date={date}"),
                eq(HttpMethod.GET),
                any(),
                eq(AppointmentSummary[].class),
                eq(java.util.Map.of("date", date))))
                .thenReturn(ResponseEntity.ok(appointments));

        List<AppointmentSummary> response = appointmentClient.getBookedAppointmentsForDate(date);

        assertThat(response).hasSize(1);
    }
}
