package com.medibook.review.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class ProviderClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProviderClient providerClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(providerClient, "internalSecret", "test-secret");
    }

    @Test
    void updateProviderRating_success() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class),
                any(java.util.Map.class)))
                .thenReturn(ResponseEntity.ok().build());

        providerClient.updateProviderRating(1L, 4.5);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PUT), any(), eq(Void.class), any(java.util.Map.class));
    }
}
