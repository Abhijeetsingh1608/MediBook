package com.medibook.notification.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InternalRequestFilterTest {

    private InternalRequestFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new InternalRequestFilter();
        ReflectionTestUtils.setField(filter, "internalSecret", "secret");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_allowsSwaggerRoutes() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_rejectsInvalidSecret() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/notifications");
        when(request.getHeader("X-Internal-Secret")).thenReturn("wrong");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
    }

    @Test
    void doFilterInternal_allowsValidSecret() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/notifications");
        when(request.getHeader("X-Internal-Secret")).thenReturn("secret");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
