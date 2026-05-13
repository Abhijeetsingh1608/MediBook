package com.medibook.schedule.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestClientConfigTest {

    @Test
    void restTemplate_returnsInstance() {
        RestClientConfig config = new RestClientConfig();
        RestTemplate restTemplate = config.loadBalancedRestTemplate();
        assertThat(restTemplate).isNotNull();
    }
}
