package com.medibook.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secret = Base64.getEncoder().encodeToString(key.getEncoded());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
    }

    @Test
    void extractUsername_success() {
        String token = Jwts.builder()
                .setSubject("test@example.com")
                .signWith(key)
                .compact();

        String result = jwtService.extractUsername(token);
        assertThat(result).isEqualTo("test@example.com");
    }

    @Test
    void extractRole_success() {
        String token = Jwts.builder()
                .setClaims(Map.of("role", "ADMIN"))
                .signWith(key)
                .compact();

        String result = jwtService.extractRole(token);
        assertThat(result).isEqualTo("ADMIN");
    }

    @Test
    void extractUserId_success() {
        String token = Jwts.builder()
                .setClaims(Map.of("userId", 123L))
                .signWith(key)
                .compact();

        Long result = jwtService.extractUserId(token);
        assertThat(result).isEqualTo(123L);
    }
    
    @Test
    void extractUserId_intValue_success() {
        String token = Jwts.builder()
                .setClaims(Map.of("userId", 123))
                .signWith(key)
                .compact();

        Long result = jwtService.extractUserId(token);
        assertThat(result).isEqualTo(123L);
    }
}
