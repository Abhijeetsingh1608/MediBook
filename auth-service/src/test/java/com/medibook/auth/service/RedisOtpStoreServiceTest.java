package com.medibook.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.auth.entity.AuthOtp;
import com.medibook.auth.entity.AuthOtpPurpose;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisOtpStoreServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisOtpStoreService redisOtpStoreService;

    @Test
    void saveAuthOtp_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        AuthOtp authOtp = AuthOtp.builder()
                .email("test@example.com")
                .purpose(AuthOtpPurpose.LOGIN)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        boolean result = redisOtpStoreService.saveAuthOtp(authOtp);

        assertThat(result).isTrue();
        verify(valueOperations).set(anyString(), anyString(), any());
    }

    @Test
    void getAuthOtp_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("{}");
        AuthOtp authOtp = AuthOtp.builder().email("test@example.com").build();
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(authOtp);

        Optional<AuthOtp> result = redisOtpStoreService.getAuthOtp("test@example.com", AuthOtpPurpose.LOGIN);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void deleteAuthOtp_success() {
        boolean result = redisOtpStoreService.deleteAuthOtp("test@example.com", AuthOtpPurpose.LOGIN);
        assertThat(result).isTrue();
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void savePasswordResetOtp_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        com.medibook.auth.entity.PasswordResetOtp otp = com.medibook.auth.entity.PasswordResetOtp.builder()
                .email("test@example.com")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        boolean result = redisOtpStoreService.savePasswordResetOtp(otp);

        assertThat(result).isTrue();
        verify(valueOperations).set(anyString(), anyString(), any());
    }

    @Test
    void getPasswordResetOtp_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("{}");
        com.medibook.auth.entity.PasswordResetOtp otp = com.medibook.auth.entity.PasswordResetOtp.builder().email("test@example.com").build();
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(otp);

        Optional<com.medibook.auth.entity.PasswordResetOtp> result = redisOtpStoreService.getPasswordResetOtp("test@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void updatePasswordResetOtp_success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        com.medibook.auth.entity.PasswordResetOtp otp = com.medibook.auth.entity.PasswordResetOtp.builder()
                .email("test@example.com")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        boolean result = redisOtpStoreService.updatePasswordResetOtp(otp);

        assertThat(result).isTrue();
    }

    @Test
    void deletePasswordResetOtp_success() {
        boolean result = redisOtpStoreService.deletePasswordResetOtp("test@example.com");
        assertThat(result).isTrue();
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void getValue_returnsEmptyOnJsonError() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("invalid-json");
        when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "error"));

        Optional<AuthOtp> result = redisOtpStoreService.getAuthOtp("test@example.com", AuthOtpPurpose.LOGIN);

        assertThat(result).isEmpty();
    }

    @Test
    void saveValue_returnsFalseOnRedisError() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        org.mockito.Mockito.doThrow(new org.springframework.dao.QueryTimeoutException("timeout"))
                .when(valueOperations).set(anyString(), anyString(), any());

        AuthOtp authOtp = AuthOtp.builder()
                .email("test@example.com")
                .purpose(AuthOtpPurpose.LOGIN)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        boolean result = redisOtpStoreService.saveAuthOtp(authOtp);

        assertThat(result).isFalse();
    }
}
