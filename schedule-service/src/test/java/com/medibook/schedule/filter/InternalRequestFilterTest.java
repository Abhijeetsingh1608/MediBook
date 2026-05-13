package com.medibook.schedule.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        ReflectionTestUtils.setField(filter, "internalSecret", "test-secret");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternal_validSecret_continuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/internal/test");
        when(request.getHeader("X-Internal-Secret")).thenReturn("test-secret");

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidSecret_returnsUnauthorized() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/internal/test");
        when(request.getHeader("X-Internal-Secret")).thenReturn("wrong-secret");
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilterInternal_nonInternalPath_continuesChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/schedules");
        when(request.getHeader("X-Internal-Secret")).thenReturn("test-secret");

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
