package com.medibook.gateway.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.medibook.gateway.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationFilter, "internalSecret", "test-secret");
    }

    @Test
    void doFilterInternal_optionsRequest_continuesChain() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("OPTIONS");
        when(request.getRequestURI()).thenReturn("/api/any");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_publicPath_continuesChainWithInternalSecret() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(MutableHttpServletRequest.class), eq(response));
    }

    @Test
    void doFilterInternal_missingAuthHeader_returns401() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/appointments");
        when(request.getHeader("Authorization")).thenReturn(null);
        
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        assertThat(sw.toString()).contains("Missing or invalid Authorization header");
    }

    @Test
    void doFilterInternal_validToken_setsHeadersAndContinues() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/appointments");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        
        when(jwtService.extractUsername("valid-token")).thenReturn("user@example.com");
        when(jwtService.extractRole("valid-token")).thenReturn("PATIENT");
        when(jwtService.extractUserId("valid-token")).thenReturn(10L);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(MutableHttpServletRequest.class), eq(response));
    }

    @Test
    void doFilterInternal_invalidToken_returns401() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/appointments");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        
        when(jwtService.extractUsername("invalid-token")).thenThrow(new RuntimeException("Invalid"));
        
        StringWriter sw = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(sw));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        assertThat(sw.toString()).contains("Invalid token");
    }
}
