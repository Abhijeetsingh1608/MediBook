package com.medibook.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medibook.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

class SecurityConfigTest {

    @Test
    void authenticationManager_returnsManager() throws Exception {
        JwtAuthenticationFilter jwtFilter = mock(JwtAuthenticationFilter.class);
        OAuth2LoginSuccessHandler successHandler = mock(OAuth2LoginSuccessHandler.class);
        OAuth2LoginFailureHandler failureHandler = mock(OAuth2LoginFailureHandler.class);
        ClientRegistrationRepository clientRepo = mock(ClientRegistrationRepository.class);
        com.medibook.auth.filter.InternalRequestFilter internalRequestFilter = mock(com.medibook.auth.filter.InternalRequestFilter.class);

        SecurityConfig config = new SecurityConfig(
                jwtFilter, successHandler, failureHandler, clientRepo, internalRequestFilter);

        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        assertThat(config.authenticationManager(authConfig)).isEqualTo(manager);
    }
}
