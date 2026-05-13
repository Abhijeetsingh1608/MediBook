package com.medibook.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.auth.dto.ApiMessage;
import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.dto.ForgotPasswordRequest;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.RegisterRequest;
import com.medibook.auth.dto.ResetPasswordRequest;
import com.medibook.auth.dto.VerifyOtpRequest;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import com.medibook.auth.service.AuthService;
import com.medibook.auth.service.OtpService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new com.medibook.auth.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /register - success")
    void register_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password")
                .role(UserRole.PATIENT)
                .build();

        User user = User.builder().userId(1L).email("test@example.com").build();
        when(authService.register(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /login - success")
    void login_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        doNothing().when(authService).login(any(LoginRequest.class));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.otpSent").value(true));
    }

    @Test
    @DisplayName("POST /verify-email - success")
    void verifyEmail_success() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        AuthResponse response = AuthResponse.builder().token("jwt-token").build();
        when(authService.verifyEmail(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void resendVerificationOtp_success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification OTP sent successfully"));
    }

    @Test
    void forgotPassword_success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully to registered email"));
    }

    @Test
    void verifyOtp_success() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP verified successfully"));
    }

    @Test
    void resetPassword_success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("newPass123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void oauth2Success_success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/oauth2/success")
                        .param("token", "dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-token"));
    }

    @Test
    @DisplayName("GET /users/{userId} - success")
    void getUserById_success() throws Exception {
        User user = User.builder().userId(1L).email("test@example.com").build();
        when(authService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("PUT /users/{userId} - success")
    void updateProfile_success() throws Exception {
        User user = User.builder().fullName("Updated Name").build();
        when(authService.updateProfile(anyLong(), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/v1/auth/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /users/{userId}/role - success")
    void updateUserRole_success() throws Exception {
        Map<String, String> request = Map.of("role", "PROVIDER");
        User user = User.builder().role(UserRole.PROVIDER).build();
        when(authService.updateUserRole(eq(1L), eq(UserRole.PROVIDER))).thenReturn(user);

        mockMvc.perform(put("/api/v1/auth/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROVIDER"));
    }

    @Test
    @DisplayName("PUT /users/{userId}/deactivate - success")
    void deactivateUser_success() throws Exception {
        doNothing().when(authService).deactivateUser(1L);

        mockMvc.perform(put("/api/v1/auth/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deactivated successfully"));
    }

    @Test
    void getAllUsers_success() throws Exception {
        when(authService.getAllUsers()).thenReturn(List.of(User.builder().userId(1L).build()));

        mockMvc.perform(get("/api/v1/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void changePassword_success() throws Exception {
        Map<String, String> request = Map.of("password", "newPass");
        when(authService.changePassword(eq(1L), anyString())).thenReturn(new User());

        mockMvc.perform(put("/api/v1/auth/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void activateUser_success() throws Exception {
        when(authService.activateUser(1L)).thenReturn(new User());

        mockMvc.perform(put("/api/v1/auth/users/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /verify-login-otp - success")
    void verifyLoginOtp_success() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        AuthResponse response = AuthResponse.builder().token("jwt-token").build();
        when(authService.verifyLoginOtp(anyString(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-login-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("GET /me - success")
    void getCurrentUser_success() throws Exception {
        User user = User.builder().email("test@example.com").build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");
        when(authService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/v1/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("PUT /users/{userId}/role - failure (role required)")
    void updateUserRole_roleRequired() throws Exception {
        Map<String, String> request = Map.of("role", "");

        mockMvc.perform(put("/api/v1/auth/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
