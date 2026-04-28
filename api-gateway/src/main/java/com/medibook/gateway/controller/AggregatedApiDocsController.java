package com.medibook.gateway.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aggregate")
@RequiredArgsConstructor
/*
 * This controller gathers OpenAPI docs from each microservice.
 * It gives one central gateway URL for Swagger dropdown usage.
 */
public class AggregatedApiDocsController {

    private static final Map<String, String> SERVICE_DOC_PATHS = Map.of(
            "auth", "/v3/api-docs",
            "providers", "/v3/api-docs",
            "slots", "/v3/api-docs",
            "appointments", "/v3/api-docs",
            "reviews", "/v3/api-docs",
            "records", "/v3/api-docs",
            "notifications", "/v3/api-docs",
            "payments", "/v3/api-docs");

    @Value("${app.swagger.services.auth}")
    private String authServiceBaseUrl;

    @Value("${app.swagger.services.providers}")
    private String providerServiceBaseUrl;

    @Value("${app.swagger.services.slots}")
    private String scheduleServiceBaseUrl;

    @Value("${app.swagger.services.appointments}")
    private String appointmentServiceBaseUrl;

    @Value("${app.swagger.services.reviews}")
    private String reviewServiceBaseUrl;

    @Value("${app.swagger.services.records}")
    private String recordServiceBaseUrl;

    @Value("${app.swagger.services.notifications}")
    private String notificationServiceBaseUrl;

    @Value("${app.swagger.services.payments}")
    private String paymentServiceBaseUrl;

    @GetMapping("/{service}/openapi")
    /*
     * This endpoint returns the requested service Swagger JSON.
     * Swagger UI uses this to build the service dropdown in one place.
     */
    public ResponseEntity<String> getServiceDocs(@PathVariable String service)
            throws IOException, InterruptedException {

        String docPath = SERVICE_DOC_PATHS.get(service);
        if (docPath == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"Unknown service for Swagger docs\"}");
        }

        String baseUrl = resolveBaseUrl(service);
        URL targetUrl = new URL(baseUrl + docPath);
        HttpURLConnection connection =
                (HttpURLConnection) targetUrl.openConnection(Proxy.NO_PROXY);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.connect();

        int status = connection.getResponseCode();
        String body;
        if (status >= 200 && status < 400) {
            body = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } else if (connection.getErrorStream() != null) {
            body = new String(connection.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            body = "";
        }

        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body);
    }

    @GetMapping("/ping")
    /*
     * This small endpoint helps confirm that aggregate routes are reachable.
     */
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }

    /*
     * This helper chooses the correct service base URL for the requested docs.
     */
    private String resolveBaseUrl(String service) {
        return switch (service) {
            case "auth" -> authServiceBaseUrl;
            case "providers" -> providerServiceBaseUrl;
            case "slots" -> scheduleServiceBaseUrl;
            case "appointments" -> appointmentServiceBaseUrl;
            case "reviews" -> reviewServiceBaseUrl;
            case "records" -> recordServiceBaseUrl;
            case "notifications" -> notificationServiceBaseUrl;
            case "payments" -> paymentServiceBaseUrl;
            default -> "";
        };
    }
}
