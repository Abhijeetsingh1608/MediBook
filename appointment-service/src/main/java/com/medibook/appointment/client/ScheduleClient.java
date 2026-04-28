package com.medibook.appointment.client;

import com.medibook.appointment.dto.ScheduleSlotResponse;
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
/*
 * This client is used when this service needs to call another microservice.
 * It helps connect modules without putting that logic in controller directly.
 */
public class ScheduleClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

/*
 * This dependency is required for the working of this class.
 */
    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

/*
 * This method fetches data using a specific id or filter value.
 * It is useful when we want a particular record instead of full list.
 */
    public ScheduleSlotResponse getSlotById(Long slotId) {
        HttpHeaders headers = createInternalHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                        "http://SCHEDULE-SERVICE/api/v1/slots/{slotId}",
                        HttpMethod.GET,
                        entity,
                        ScheduleSlotResponse.class,
                        Map.of("slotId", slotId))
                .getBody();
    }

/*
 * This method updates existing data with new values.
 * It is used when user edits profile, status, or any stored details.
 */
    public void updateSlotStatus(Long slotId, String status) {
        HttpHeaders headers = createInternalHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(
                "http://SCHEDULE-SERVICE/api/v1/slots/{slotId}/status?status={status}",
                HttpMethod.PUT,
                entity,
                Void.class,
                Map.of("slotId", slotId, "status", status));
    }

/*
 * This method is used to create and save new data.
 * It takes input, prepares the object, and stores it in database or next layer.
 */
    private HttpHeaders createInternalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_SECRET_HEADER, internalSecret);
        return headers;
    }
}
