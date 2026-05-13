package com.medibook.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RazorpayClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RazorpayClient razorpayClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(razorpayClient, "keyId", "rzp_test");
        ReflectionTestUtils.setField(razorpayClient, "keySecret", "secret");
        ReflectionTestUtils.setField(razorpayClient, "ordersUrl", "https://api.razorpay.com/v1/orders");
    }

    @Test
    void createOrder_success() {
        Map<String, Object> response = Map.of("id", "order_123");
        when(restTemplate.postForObject(eq("https://api.razorpay.com/v1/orders"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Map result = razorpayClient.createOrder(1000L, "INR", "receipt_1");

        assertThat(result.get("id")).isEqualTo("order_123");
    }

    @Test
    void getKeyId_returnsValue() {
        assertThat(razorpayClient.getKeyId()).isEqualTo("rzp_test");
    }

    @Test
    void validateCredentials_throwsWhenMissing() {
        ReflectionTestUtils.setField(razorpayClient, "keyId", "");
        assertThatThrownBy(() -> razorpayClient.getKeyId())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Razorpay key id/secret is not configured");
    }
}
