package com.medibook.notification.client;

import com.medibook.notification.dto.UserSummary;
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
public class AuthClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

    public UserSummary getUserById(Long userId) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        return restTemplate.exchange(
                        "http://AUTH-SERVICE/api/v1/internal/users/{userId}",
                        HttpMethod.GET,
                        entity,
                        UserSummary.class,
                        Map.of("userId", userId))
                .getBody();
    }

    public List<UserSummary> getUsersByRole(String role) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        UserSummary[] response = restTemplate.exchange(
                        "http://AUTH-SERVICE/api/v1/internal/users/role/{role}",
                        HttpMethod.GET,
                        entity,
                        UserSummary[].class,
                        Map.of("role", role))
                .getBody();
        return response == null ? Collections.emptyList() : Arrays.asList(response);
    }

    private HttpHeaders createInternalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_SECRET_HEADER, internalSecret);
        return headers;
    }
}
