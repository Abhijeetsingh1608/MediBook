package com.medibook.review.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class ReviewConfigTest {

    @Test
    void restTemplateBean_exists() {
        RestClientConfig config = new RestClientConfig();
        RestTemplate restTemplate = config.loadBalancedRestTemplate();
        assertThat(restTemplate).isNotNull();
    }
}
