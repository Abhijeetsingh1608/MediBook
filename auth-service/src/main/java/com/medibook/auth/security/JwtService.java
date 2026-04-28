package com.medibook.auth.security;

import com.medibook.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
/*
 * This class is an important part of the JwtService flow.
 * It supports the working of this module in the project.
 */
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getUserId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    public Long extractUserId(String token) {
        Object value = extractAllClaims(token).get("userId");
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        return Long.valueOf(String.valueOf(value));
    }

/*
 * This helper method returns true or false based on a condition.
 * It keeps validation logic reusable and clean.
 */
    public boolean isTokenValid(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username)
                && !extractAllClaims(token).getExpiration().before(new Date());
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

/*
 * This method is part of the main flow of this class.
 * It helps complete one specific task of this module.
 */
    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
