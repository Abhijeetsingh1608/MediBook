package com.medibook.schedule.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
/*
 * This filter checks each request before it reaches the main logic.
 * It is useful for security, headers, or internal request validation.
 */
public class InternalRequestFilter extends OncePerRequestFilter {

    @Value("${app.internal.secret}")
    private String internalSecret;

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

        if (path.startsWith("/swagger-ui")
                || path.contains("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        String incomingSecret = request.getHeader("X-Internal-Secret");

        if (incomingSecret == null || !incomingSecret.equals(internalSecret)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Direct access is not allowed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
