package com.medibook.gateway.filter;

import com.medibook.gateway.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
/*
 * This filter checks each request before it reaches the main logic.
 * It is useful for security, headers, or internal request validation.
 */
public class AuthenticationFilter extends OncePerRequestFilter {

/*
 * This service dependency is used to reuse business logic from another class.
 */
    private final JwtService jwtService;

    @Value("${app.internal.secret}")
    private String internalSecret;

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/resend-verification-otp",
            "/api/v1/auth/verify-login-otp",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/oauth2/success",
            "/oauth2/",
            "/login/",
            "/aggregate/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicPath(path)) {
            MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
            mutableRequest.putHeader("X-Internal-Secret", internalSecret);
            filterChain.doFilter(mutableRequest, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);
            Long userId = jwtService.extractUserId(token);

            MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
            mutableRequest.putHeader("X-User-Id", String.valueOf(userId));
            mutableRequest.putHeader("X-User-Email", email);
            mutableRequest.putHeader("X-User-Role", role);
            mutableRequest.putHeader("X-Internal-Secret", internalSecret);

            filterChain.doFilter(mutableRequest, response);
        } catch (Exception ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid token");
        }
    }

/*
 * This helper method returns true or false based on a condition.
 * It keeps validation logic reusable and clean.
 */
    private boolean isPublicPath(String path) {
        return path.contains("/v3/api-docs") || PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
