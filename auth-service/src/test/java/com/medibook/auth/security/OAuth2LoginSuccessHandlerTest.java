package com.medibook.auth.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import com.medibook.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;
    private OAuth2User oauth2User;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
        oauth2User = mock(OAuth2User.class);
        ReflectionTestUtils.setField(handler, "successRedirectUrl", "http://localhost:5173/oauth2/redirect");
    }

    @Test
    void onAuthenticationSuccess_redirectsWithToken() throws Exception {
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("TestUser");
        
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .fullName("TestUser")
                .role(UserRole.PATIENT)
                .build();
        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .user(user)
                .build();
        
        when(authService.loginWithGoogle("test@example.com", "TestUser")).thenReturn(authResponse);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("http://localhost:5173/oauth2/redirect?token=jwt-token&userId=1&role=PATIENT&name=TestUser&email=test@example.com");
    }
}
