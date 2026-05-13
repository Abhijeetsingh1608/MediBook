package com.medibook.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", expirationMs);
    }

    @Test
    void generateAndValidateToken() {
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .role(UserRole.PATIENT)
                .build();

        String token = jwtService.generateToken(user);
        assertThat(token).isNotEmpty();

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("PATIENT");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.isTokenValid(token, "test@example.com")).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForInvalidUser() {
        User user = User.builder()
                .userId(1L)
                .email("test@example.com")
                .role(UserRole.PATIENT)
                .build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, "wrong@example.com")).isFalse();
    }
}
