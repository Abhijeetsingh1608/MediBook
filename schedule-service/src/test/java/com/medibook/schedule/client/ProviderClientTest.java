package com.medibook.schedule.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medibook.schedule.dto.ProviderSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class ProviderClientTest {

    private RestTemplate restTemplate;
    private ProviderClient providerClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        providerClient = new ProviderClient(restTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(providerClient, "internalSecret", "test-secret");
    }

    @Test
    void getProviderById_success() {
        ProviderSummary summary = ProviderSummary.builder().providerId(1L).build();
        org.springframework.http.ResponseEntity<ProviderSummary> response = 
            new org.springframework.http.ResponseEntity<>(summary, org.springframework.http.HttpStatus.OK);
        
        when(restTemplate.exchange(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(org.springframework.http.HttpMethod.GET),
                org.mockito.ArgumentMatchers.any(org.springframework.http.HttpEntity.class),
                org.mockito.ArgumentMatchers.eq(ProviderSummary.class),
                org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(response);

        ProviderSummary result = providerClient.getProviderById(1L);
        assertThat(result.getProviderId()).isEqualTo(1L);
    }
}
