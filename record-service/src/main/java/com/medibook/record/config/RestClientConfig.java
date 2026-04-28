package com.medibook.record.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
/*
 * This configuration class sets up important project settings for RestClientConfig.
 * Beans and reusable configurations are usually placed here.
 */
public class RestClientConfig {

    @Bean
    @LoadBalanced
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
