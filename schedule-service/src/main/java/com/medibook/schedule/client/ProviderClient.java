package com.medibook.schedule.client;

import com.medibook.schedule.dto.ProviderSummary;
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
public class ProviderClient {

    private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    private final RestTemplate restTemplate;

    @Value("${app.internal.secret}")
    private String internalSecret;

    public ProviderSummary getProviderById(Long providerId) {
        HttpEntity<Void> entity = new HttpEntity<>(createInternalHeaders());
        return restTemplate.exchange(
                        "http://PROVIDER-SERVICE/api/v1/providers/{providerId}",
                        HttpMethod.GET,
                        entity,
                        ProviderSummary.class,
                        Map.of("providerId", providerId))
                .getBody();
    }

    private HttpHeaders createInternalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_SECRET_HEADER, internalSecret);
        return headers;
    }
}
