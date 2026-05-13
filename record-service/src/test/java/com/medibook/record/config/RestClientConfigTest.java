package com.medibook.record.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class RestClientConfigTest {

    @Test
    void restTemplate_returnsInstance() {
        RestTemplate restTemplate = new RestClientConfig().restTemplate();

        assertThat(restTemplate).isNotNull();
    }
}
