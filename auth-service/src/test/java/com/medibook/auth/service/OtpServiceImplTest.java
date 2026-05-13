package com.medibook.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.auth.entity.PasswordResetOtp;
import com.medibook.auth.entity.User;
import com.medibook.auth.repository.PasswordResetOtpRepository;
import com.medibook.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisOtpStoreService redisOtpStoreService;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "otpExpirationMinutes", 5L);
    }

    @Test
    void generateAndSendOtp_success() {
        User user = User.builder().email("test@example.com").fullName("Test").build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(redisOtpStoreService.savePasswordResetOtp(any())).thenReturn(true);

        otpService.generateAndSendOtp("test@example.com");

        verify(emailService).sendOtpEmail(anyString(), anyString(), anyString());
    }

    @Test
    void verifyOtp_success() {
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(redisOtpStoreService.getPasswordResetOtp("test@example.com")).thenReturn(Optional.of(otp));

        otpService.verifyOtp("test@example.com", "123456");

        assertThat(otp.isVerified()).isTrue();
    }

    @Test
    void resetPassword_success() {
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        User user = User.builder().email("test@example.com").build();

        when(redisOtpStoreService.getPasswordResetOtp("test@example.com")).thenReturn(Optional.of(otp));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-pass");

        otpService.resetPassword("test@example.com", "123456", "new-pass");

        assertThat(user.getPassword()).isEqualTo("encoded-pass");
        assertThat(otp.isUsed()).isTrue();
    }

    @Test
    void verifyOtp_invalidOtp_throwsException() {
        PasswordResetOtp otp = PasswordResetOtp.builder()
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        when(redisOtpStoreService.getPasswordResetOtp("test@example.com")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpService.verifyOtp("test@example.com", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid OTP");
    }
}
