package com.medibook.notification.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.medibook.notification.dto.ProviderSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class ProviderClientTest {

    private RestTemplate restTemplate;
    private ProviderClient providerClient;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        providerClient = new ProviderClient(restTemplate);
        ReflectionTestUtils.setField(providerClient, "internalSecret", "secret");
    }

    @Test
    void getProviderById_returnsBody() {
        ProviderSummary provider = ProviderSummary.builder().providerId(3L).fullName("Dr. Test").build();
        when(restTemplate.exchange(
                eq("http://PROVIDER-SERVICE/api/v1/providers/{providerId}"),
                eq(HttpMethod.GET),
                any(),
                eq(ProviderSummary.class),
                eq(java.util.Map.of("providerId", 3L))))
                .thenReturn(ResponseEntity.ok(provider));

        ProviderSummary response = providerClient.getProviderById(3L);

        assertThat(response).isEqualTo(provider);
    }
}
