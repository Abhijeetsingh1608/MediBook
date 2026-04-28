package com.medibook.payment.client;

import com.medibook.payment.dto.AppointmentResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
/*
 * This client is used when this service needs to call another microservice.
 * It helps connect modules without putting that logic in controller directly.
 */
public class AppointmentClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

/*
 * This dependency is required for the working of this class.
 */
    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public AppointmentClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

/*
 * This method fetches data using a specific id or filter value.
 * It is useful when we want a particular record instead of full list.
 */
    public AppointmentResponse getAppointmentById(Long appointmentId) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        return restTemplate.exchange(
                        "http://APPOINTMENT-SERVICE/api/v1/appointments/{appointmentId}",
                        HttpMethod.GET,
                        entity,
                        AppointmentResponse.class,
                        Map.of("appointmentId", appointmentId))
                .getBody();
    }

/*
 * This method is used when payment setup fails before checkout opens.
 * It tells appointment-service to free the booked slot immediately.
 */
    public void releaseFailedPaymentBooking(Long appointmentId) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        restTemplate.exchange(
                "http://APPOINTMENT-SERVICE/api/v1/appointments/{appointmentId}/internal-release",
                HttpMethod.PUT,
                entity,
                Void.class,
                Map.of("appointmentId", appointmentId));
    }

/*
 * This method is used after payment success to make sure
 * the appointment stays active and booked in the appointment module.
 */
    public void activatePaidAppointment(Long appointmentId) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        restTemplate.exchange(
                "http://APPOINTMENT-SERVICE/api/v1/appointments/{appointmentId}/internal-activate",
                HttpMethod.PUT,
                entity,
                Void.class,
                Map.of("appointmentId", appointmentId));
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
