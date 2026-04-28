package com.medibook.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.auth.entity.AuthOtp;
import com.medibook.auth.entity.AuthOtpPurpose;
import com.medibook.auth.entity.PasswordResetOtp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This helper service keeps short-lived OTP data in Redis.
 * Redis is faster than database for temporary values like OTPs,
 * but this class also lets callers fall back safely if Redis is unavailable.
 */
public class RedisOtpStoreService {

    private static final String AUTH_OTP_KEY_PREFIX = "medibook:auth:otp:";
    private static final String PASSWORD_RESET_OTP_KEY_PREFIX = "medibook:password-reset:otp:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /*
     * This method stores login or email verification OTP in Redis.
     * It returns true if Redis save succeeded, otherwise false.
     */
    public boolean saveAuthOtp(AuthOtp authOtp) {
        return saveValue(buildAuthOtpKey(authOtp.getEmail(), authOtp.getPurpose()), authOtp, authOtp.getExpiryTime());
    }

    /*
     * This method fetches login or email verification OTP from Redis.
     * If Redis has no usable value, it returns empty.
     */
    public Optional<AuthOtp> getAuthOtp(String email, AuthOtpPurpose purpose) {
        return getValue(buildAuthOtpKey(email, purpose), AuthOtp.class);
    }

    /*
     * This method removes used login or verification OTP from Redis.
     * It returns true when delete succeeds, otherwise false.
     */
    public boolean deleteAuthOtp(String email, AuthOtpPurpose purpose) {
        return deleteValue(buildAuthOtpKey(email, purpose));
    }

    /*
     * This method stores forgot-password OTP data in Redis.
     * It returns true if Redis save succeeded, otherwise false.
     */
    public boolean savePasswordResetOtp(PasswordResetOtp passwordResetOtp) {
        return saveValue(buildPasswordResetOtpKey(passwordResetOtp.getEmail()), passwordResetOtp, passwordResetOtp.getExpiryTime());
    }

    /*
     * This method fetches forgot-password OTP details from Redis.
     * If Redis has no usable value, it returns empty.
     */
    public Optional<PasswordResetOtp> getPasswordResetOtp(String email) {
        return getValue(buildPasswordResetOtpKey(email), PasswordResetOtp.class);
    }

    /*
     * This method updates forgot-password OTP state in Redis
     * after verification succeeds.
     */
    public boolean updatePasswordResetOtp(PasswordResetOtp passwordResetOtp) {
        return saveValue(buildPasswordResetOtpKey(passwordResetOtp.getEmail()), passwordResetOtp, passwordResetOtp.getExpiryTime());
    }

    /*
     * This method removes used forgot-password OTP from Redis.
     * It returns true when delete succeeds, otherwise false.
     */
    public boolean deletePasswordResetOtp(String email) {
        return deleteValue(buildPasswordResetOtpKey(email));
    }

    private String buildAuthOtpKey(String email, AuthOtpPurpose purpose) {
        return AUTH_OTP_KEY_PREFIX + purpose.name() + ":" + email.toLowerCase();
    }

    private String buildPasswordResetOtpKey(String email) {
        return PASSWORD_RESET_OTP_KEY_PREFIX + email.toLowerCase();
    }

    private <T> boolean saveValue(String key, T value, LocalDateTime expiryTime) {
        try {
            Duration ttl = calculateTtl(expiryTime);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
            return true;
        } catch (JsonProcessingException | DataAccessException ex) {
            return false;
        }
    }

    private <T> Optional<T> getValue(String key, Class<T> type) {
        try {
            String rawValue = redisTemplate.opsForValue().get(key);
            if (rawValue == null || rawValue.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(rawValue, type));
        } catch (JsonProcessingException | DataAccessException ex) {
            return Optional.empty();
        }
    }

    private boolean deleteValue(String key) {
        try {
            redisTemplate.delete(key);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private Duration calculateTtl(LocalDateTime expiryTime) {
        Duration ttl = Duration.between(LocalDateTime.now(), expiryTime);
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }
}
