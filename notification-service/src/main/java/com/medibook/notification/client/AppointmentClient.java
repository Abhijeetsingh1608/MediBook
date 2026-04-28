package com.medibook.notification.client;

import com.medibook.notification.dto.AppointmentSummary;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AppointmentClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

    public List<AppointmentSummary> getBookedAppointmentsForDate(LocalDate date) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        AppointmentSummary[] response = restTemplate.exchange(
                        "http://APPOINTMENT-SERVICE/api/v1/appointments/internal/booked-by-date?date={date}",
                        HttpMethod.GET,
                        entity,
                        AppointmentSummary[].class,
                        Map.of("date", date))
                .getBody();
        return response == null ? Collections.emptyList() : Arrays.asList(response);
    }

    private HttpHeaders createInternalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_SECRET_HEADER, internalSecret);
        return headers;
    }
}
