package com.medibook.gateway.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

class AggregatedApiDocsControllerTest {

    private static final AtomicBoolean FACTORY_SET = new AtomicBoolean(false);

    @BeforeAll
    static void registerUrlHandlerFactory() {
        if (FACTORY_SET.compareAndSet(false, true)) {
            URL.setURLStreamHandlerFactory(new StubHttpUrlStreamHandlerFactory());
        }
    }

    @Test
    void getServiceDocs_returnsNotFoundForUnknownService() throws Exception {
        AggregatedApiDocsController controller = configuredController();

        ResponseEntity<String> response = controller.getServiceDocs("unknown");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Unknown service");
    }

    @Test
    void getServiceDocs_returnsSuccessfulSwaggerPayload() throws Exception {
        StubHttpURLConnection.responseCode = 200;
        StubHttpURLConnection.inputBody = "{\"openapi\":\"3.0.1\"}";
        StubHttpURLConnection.errorBody = "";
        AggregatedApiDocsController controller = configuredController();

        ResponseEntity<String> response = controller.getServiceDocs("auth");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("Content-Type")).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getBody()).contains("openapi");
    }

    @Test
    void getServiceDocs_returnsErrorStreamBodyForFailures() throws Exception {
        StubHttpURLConnection.responseCode = 503;
        StubHttpURLConnection.inputBody = "";
        StubHttpURLConnection.errorBody = "{\"message\":\"down\"}";
        AggregatedApiDocsController controller = configuredController();

        ResponseEntity<String> response = controller.getServiceDocs("payments");

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).contains("down");
    }

    @Test
    void ping_returnsOk() {
        ResponseEntity<String> response = configuredController().ping();

        assertThat(response.getBody()).isEqualTo("ok");
    }

    private AggregatedApiDocsController configuredController() {
        AggregatedApiDocsController controller = new AggregatedApiDocsController();
        ReflectionTestUtils.setField(controller, "authServiceBaseUrl", "http://auth-service");
        ReflectionTestUtils.setField(controller, "providerServiceBaseUrl", "http://provider-service");
        ReflectionTestUtils.setField(controller, "scheduleServiceBaseUrl", "http://schedule-service");
        ReflectionTestUtils.setField(controller, "appointmentServiceBaseUrl", "http://appointment-service");
        ReflectionTestUtils.setField(controller, "reviewServiceBaseUrl", "http://review-service");
        ReflectionTestUtils.setField(controller, "recordServiceBaseUrl", "http://record-service");
        ReflectionTestUtils.setField(controller, "notificationServiceBaseUrl", "http://notification-service");
        ReflectionTestUtils.setField(controller, "paymentServiceBaseUrl", "http://payment-service");
        return controller;
    }

    private static final class StubHttpUrlStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (!"http".equals(protocol)) {
                return null;
            }
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) {
                    return new StubHttpURLConnection(url);
                }

                @Override
                protected URLConnection openConnection(URL url, Proxy proxy) {
                    return new StubHttpURLConnection(url);
                }
            };
        }
    }

    private static final class StubHttpURLConnection extends HttpURLConnection {

        private static int responseCode = 200;
        private static String inputBody = "";
        private static String errorBody = "";

        private StubHttpURLConnection(URL url) {
            super(url);
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
        }

        @Override
        public void setRequestMethod(String method) throws ProtocolException {
            this.method = method;
        }

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(inputBody.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getErrorStream() {
            return errorBody.isEmpty()
                    ? null
                    : new ByteArrayInputStream(errorBody.getBytes(StandardCharsets.UTF_8));
        }
    }
}
