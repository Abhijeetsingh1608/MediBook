package com.medibook.notification.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestClientConfigTest {

    @Test
    void loadBalancedRestTemplate_returnsInstance() {
        RestTemplate restTemplate = new RestClientConfig().loadBalancedRestTemplate();

        assertThat(restTemplate).isNotNull();
    }
}
