package com.medibook.payment.client;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
/*
 * This client is used when this service needs to call another microservice.
 * It helps connect modules without putting that logic in controller directly.
 */
public class RazorpayClient {

/*
 * This dependency is required for the working of this class.
 */
    private final RestTemplate restTemplate;

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Value("${app.razorpay.orders-url}")
    private String ordersUrl;

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public RazorpayClient(@Qualifier("externalRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

/*
 * This method is used to create and save new data.
 * It takes input, prepares the object, and stores it in database or next layer.
 */
    public Map createOrder(Long amountPaisa, String currency, String receipt) {
        validateCredentials();

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountPaisa);
        body.put("currency", currency);
        body.put("receipt", receipt);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createHeaders());
        return restTemplate.postForObject(ordersUrl, entity, Map.class);
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String getKeySecret() {
        validateCredentials();
        return keySecret;
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String getKeyId() {
        validateCredentials();
        return keyId;
    }

/*
 * This method is used to create and save new data.
 * It takes input, prepares the object, and stores it in database or next layer.
 */
    private HttpHeaders createHeaders() {
        String credentials = keyId + ":" + keySecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        return headers;
    }

/*
 * This helper method checks rules before main logic continues.
 * It prevents invalid data or unauthorized access.
 */
    private void validateCredentials() {
        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret)) {
            throw new RuntimeException("Razorpay key id/secret is not configured");
        }
    }
}
