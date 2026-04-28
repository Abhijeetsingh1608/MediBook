package com.medibook.auth.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/*
 * This class is an important part of the OAuth2LoginFailureHandler flow.
 * It supports the working of this module in the project.
 */
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

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

        response.sendRedirect("/login?error");
    }
}
