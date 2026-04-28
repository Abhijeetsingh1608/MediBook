package com.medibook.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.entity.AuthOtp;
import com.medibook.auth.entity.AuthOtpPurpose;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import com.medibook.auth.repository.AuthOtpRepository;
import com.medibook.auth.repository.UserRepository;
import com.medibook.auth.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthOtpRepository authOtpRepository;

    @Mock
    private RedisOtpStoreService redisOtpStoreService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "otpExpirationMinutes", 5L);
        ReflectionTestUtils.setField(authService, "adminLoginSecret", "test-admin-secret");

        sampleUser = User.builder()
                .userId(1L)
                .fullName("Riya Sharma")
                .email("riya@medibook.com")
                .password("encoded-password")
                .phone("9876543210")
                .role(UserRole.PATIENT)
                .provider("LOCAL")
                .active(true)
                .emailVerified(true)
                .build();
    }

    @Test
    @DisplayName("register: success - encodes password, saves user, and sends verification OTP")
    void register_success() {
        User input = User.builder()
                .fullName("Riya Sharma")
                .email("riya@medibook.com")
                .password("Password@123")
                .phone("9876543210")
                .role(UserRole.PATIENT)
                .build();

        User saved = User.builder()
                .userId(1L)
                .fullName("Riya Sharma")
                .email("riya@medibook.com")
                .password("encoded-password")
                .phone("9876543210")
                .role(UserRole.PATIENT)
                .provider("LOCAL")
                .active(true)
                .emailVerified(false)
                .build();

        when(userRepository.existsByEmail("riya@medibook.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(redisOtpStoreService.saveAuthOtp(any(AuthOtp.class))).thenReturn(true);

        User result = authService.register(input);

        assertThat(result.getEmail()).isEqualTo("riya@medibook.com");
        assertThat(result.isEmailVerified()).isFalse();
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmailVerificationOtp(
                eq("riya@medibook.com"),
                eq("Riya Sharma"),
                org.mockito.ArgumentMatchers.argThat(otp -> otp != null && otp.matches("\\d{6}")));
        verify(authOtpRepository, never()).save(any(AuthOtp.class));
    }

    @Test
    @DisplayName("register: throws when email already exists")
    void register_duplicateEmail_throwsException() {
        User input = User.builder()
                .email("riya@medibook.com")
                .password("Password@123")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.existsByEmail("riya@medibook.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: success - sends login OTP for verified active patient")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .email("riya@medibook.com")
                .password("Password@123")
                .build();

        when(userRepository.findByEmail("riya@medibook.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(true);
        when(redisOtpStoreService.saveAuthOtp(any(AuthOtp.class))).thenReturn(true);

        authService.login(request);

        verify(emailService).sendLoginOtpEmail(
                eq("riya@medibook.com"),
                eq("Riya Sharma"),
                org.mockito.ArgumentMatchers.argThat(otp -> otp != null && otp.matches("\\d{6}")));
    }

    @Test
    @DisplayName("login: throws when admin secret key is wrong")
    void login_adminWrongSecret_throwsException() {
        sampleUser.setRole(UserRole.ADMIN);
        LoginRequest request = LoginRequest.builder()
                .email("riya@medibook.com")
                .password("Password@123")
                .adminSecretKey("wrong-secret")
                .build();

        when(userRepository.findByEmail("riya@medibook.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid admin secret key");
    }

    @Test
    @DisplayName("login: throws when email is not verified")
    void login_emailNotVerified_throwsException() {
        sampleUser.setEmailVerified(false);
        LoginRequest request = LoginRequest.builder()
                .email("riya@medibook.com")
                .password("Password@123")
                .build();

        when(userRepository.findByEmail("riya@medibook.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Password@123", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Please verify your email before login");
    }

    @Test
    @DisplayName("verifyEmail: success - marks verified and returns auth response")
    void verifyEmail_success() {
        AuthOtp authOtp = AuthOtp.builder()
                .email("riya@medibook.com")
                .otp("123456")
                .purpose(AuthOtpPurpose.EMAIL_VERIFICATION)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        sampleUser.setEmailVerified(false);

        when(userRepository.findByEmail("riya@medibook.com")).thenReturn(Optional.of(sampleUser));
        when(redisOtpStoreService.getAuthOtp("riya@medibook.com", AuthOtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(authOtp));
        when(redisOtpStoreService.deleteAuthOtp("riya@medibook.com", AuthOtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.verifyEmail("riya@medibook.com", "123456");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().isEmailVerified()).isTrue();
        verify(emailService).sendWelcomeEmail("riya@medibook.com", "Riya Sharma");
        verify(authOtpRepository, never()).save(any(AuthOtp.class));
    }

    @Test
    @DisplayName("verifyLoginOtp: throws when OTP is invalid")
    void verifyLoginOtp_invalidOtp_throwsException() {
        AuthOtp authOtp = AuthOtp.builder()
                .email("riya@medibook.com")
                .otp("123456")
                .purpose(AuthOtpPurpose.LOGIN)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(userRepository.findByEmail("riya@medibook.com")).thenReturn(Optional.of(sampleUser));
        when(redisOtpStoreService.getAuthOtp("riya@medibook.com", AuthOtpPurpose.LOGIN))
                .thenReturn(Optional.of(authOtp));

        assertThatThrownBy(() -> authService.verifyLoginOtp("riya@medibook.com", "999999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid OTP");
    }

    @Test
    @DisplayName("loginWithGoogle: creates new patient when email is not registered")
    void loginWithGoogle_newUser_createsAccount() {
        when(userRepository.findByEmail("google@medibook.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("google-oauth-user")).thenReturn("encoded-google-user");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(10L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("google-jwt");

        AuthResponse response = authService.loginWithGoogle("google@medibook.com", "Google User");

        assertThat(response.getToken()).isEqualTo("google-jwt");
        assertThat(response.getUser().getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(response.getUser().isEmailVerified()).isTrue();
        verify(emailService).sendWelcomeEmail("google@medibook.com", "Google User");
        verify(emailService).sendLoginAlertEmail("google@medibook.com", "Google User");
    }

    @Test
    @DisplayName("updateProfile: updates allowed fields and saves")
    void updateProfile_success() {
        User input = User.builder()
                .fullName("Riya S.")
                .phone("1111111111")
                .profilePicUrl("https://img.com/pic.png")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.updateProfile(1L, input);

        assertThat(result.getFullName()).isEqualTo("Riya S.");
        assertThat(result.getPhone()).isEqualTo("1111111111");
        assertThat(result.getProfilePicUrl()).isEqualTo("https://img.com/pic.png");
    }

    @Test
    @DisplayName("deactivateUser: marks account inactive")
    void deactivateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        authService.deactivateUser(1L);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> !user.isActive()));
    }
}
