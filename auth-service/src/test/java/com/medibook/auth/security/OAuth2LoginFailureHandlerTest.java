package com.medibook.auth.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2LoginFailureHandlerTest {

    @Test
    void onAuthenticationFailure_redirectsToLoginWithError() throws Exception {
        OAuth2LoginFailureHandler handler = new OAuth2LoginFailureHandler();
        ReflectionTestUtils.setField(handler, "failureRedirectUrl", "http://localhost:5173/login");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        handler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("http://localhost:5173/login?error=oauth2&message=Google login failed. Please try again.");
    }
}
