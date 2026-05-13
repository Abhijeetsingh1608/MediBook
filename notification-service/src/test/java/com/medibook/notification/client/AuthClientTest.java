package com.medibook.notification.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.notification.dto.UserSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class AuthClientTest {

    private RestTemplate restTemplate;
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        authClient = new AuthClient(restTemplate);
        ReflectionTestUtils.setField(authClient, "internalSecret", "secret");
    }

    @Test
    void getUserById_returnsBody() {
        UserSummary user = UserSummary.builder().userId(7L).email("patient@example.com").build();
        when(restTemplate.exchange(
                eq("http://AUTH-SERVICE/api/v1/internal/users/{userId}"),
                eq(HttpMethod.GET),
                any(),
                eq(UserSummary.class),
                eq(java.util.Map.of("userId", 7L))))
                .thenReturn(ResponseEntity.ok(user));

        UserSummary response = authClient.getUserById(7L);

        assertThat(response).isEqualTo(user);
    }

    @Test
    void getUsersByRole_returnsEmptyListWhenBodyMissing() {
        when(restTemplate.exchange(
                eq("http://AUTH-SERVICE/api/v1/internal/users/role/{role}"),
                eq(HttpMethod.GET),
                any(),
                eq(UserSummary[].class),
                eq(java.util.Map.of("role", "ADMIN"))))
                .thenReturn(ResponseEntity.ok(null));

        List<UserSummary> response = authClient.getUsersByRole("ADMIN");

        assertThat(response).isEmpty();
    }

    @Test
    void getUsersByRole_returnsResponseBody() {
        UserSummary[] users = {
                UserSummary.builder().userId(1L).email("admin@example.com").build()
        };
        when(restTemplate.exchange(
                eq("http://AUTH-SERVICE/api/v1/internal/users/role/{role}"),
                eq(HttpMethod.GET),
                any(),
                eq(UserSummary[].class),
                eq(java.util.Map.of("role", "ADMIN"))))
                .thenReturn(ResponseEntity.ok(users));

        List<UserSummary> response = authClient.getUsersByRole("ADMIN");

        assertThat(response).hasSize(1);
        verify(restTemplate).exchange(
                eq("http://AUTH-SERVICE/api/v1/internal/users/role/{role}"),
                eq(HttpMethod.GET),
                any(),
                eq(UserSummary[].class),
                eq(java.util.Map.of("role", "ADMIN")));
    }
}
