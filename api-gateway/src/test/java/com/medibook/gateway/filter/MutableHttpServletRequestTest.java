package com.medibook.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.junit.jupiter.api.Test;

class MutableHttpServletRequestTest {

    @Test
    void returnsCustomHeadersAndFallsBackToOriginalRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer old");
        when(request.getHeaders("Authorization")).thenReturn(Collections.enumeration(List.of("Bearer old")));
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("Authorization")));

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader("X-User-Role", "ADMIN");

        Enumeration<String> headerNames = mutableRequest.getHeaderNames();

        assertThat(mutableRequest.getHeader("Authorization")).isEqualTo("Bearer old");
        assertThat(mutableRequest.getHeader("X-User-Role")).isEqualTo("ADMIN");
        assertThat(Collections.list(mutableRequest.getHeaders("X-User-Role"))).containsExactly("ADMIN");
        assertThat(Collections.list(headerNames)).contains("Authorization", "X-User-Role");
    }
}
