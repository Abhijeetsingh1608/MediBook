package com.medibook.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
/*
 * This class is an important part of the OAuth2LoginFailureHandler flow.
 * It supports the working of this module in the project.
 */
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.failure-redirect-url}")
    private String failureRedirectUrl;

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 login failed", exception);

        String redirectUrl = UriComponentsBuilder.fromUriString(failureRedirectUrl)
                .queryParam("error", "oauth2")
                .queryParam("message", "Google login failed. Please try again.")
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
