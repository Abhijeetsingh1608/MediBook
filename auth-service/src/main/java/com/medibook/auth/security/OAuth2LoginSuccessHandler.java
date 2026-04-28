package com.medibook.auth.security;

import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
/*
 * This class is an important part of the OAuth2LoginSuccessHandler flow.
 * It supports the working of this module in the project.
 */
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

/*
 * This service dependency is used to reuse business logic from another class.
 */
    private final AuthService authService;

    @Value("${app.oauth2.success-redirect-url}")
    private String successRedirectUrl;

    @Override
/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        AuthResponse authResponse = authService.loginWithGoogle(email, name);

        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("token", authResponse.getToken())
                .queryParam("userId", authResponse.getUser().getUserId())
                .queryParam("role", authResponse.getUser().getRole().name())
                .queryParam("name", authResponse.getUser().getFullName())
                .queryParam("email", authResponse.getUser().getEmail())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
