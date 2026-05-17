package com.medibook.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
/*
 * This configuration class sets up important project settings for CorsConfig.
 * Beans and reusable configurations are usually placed here.
 */
public class CorsConfig implements WebMvcConfigurer {

    @Override
/*
 * This method is used to create and save new data.
 * It takes input, prepares the object, and stores it in database or next layer.
 */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:4174",
                        "http://127.0.0.1:4174",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "https://medibook-frontend.rho.vercel.app",
                        "https://medi-book-frontend-rho.vercel.app",
                        "https://medi-book-frontend.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }
}
