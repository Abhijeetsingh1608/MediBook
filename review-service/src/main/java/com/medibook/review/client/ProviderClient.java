package com.medibook.review.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class ProviderClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

/*
 * This dependency is required for the working of this class.
 */
    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

/*
 * This method updates existing data with new values.
 * It is used when user edits profile, status, or any stored details.
 */
    public void updateProviderRating(Long providerId, Double averageRating) {
        BigDecimal rating = BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());

        restTemplate.exchange(
                "http://PROVIDER-SERVICE/api/v1/providers/{providerId}/rating?avgRating={avgRating}",
                HttpMethod.PUT,
                entity,
                Void.class,
                Map.of("providerId", providerId, "avgRating", rating));
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
