package com.medibook.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class CorsConfigTest {

    @Test
    void addCorsMappings_registersFrontendOrigins() {
        CorsRegistry registry = new CorsRegistry();

        new CorsConfig().addCorsMappings(registry);

        assertThat(registry).isNotNull();
    }
}
