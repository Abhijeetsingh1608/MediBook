package com.medibook.auth.controller;

import com.medibook.auth.dto.ApiMessage;
import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.dto.ForgotPasswordRequest;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.dto.ResetPasswordRequest;
import com.medibook.auth.dto.VerifyOtpRequest;
import com.medibook.auth.entity.User;
import com.medibook.auth.service.AuthService;
import com.medibook.auth.service.OtpService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.medibook.auth.dto.RegisterRequest;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
/*
 * This controller handles API requests for AuthController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class AuthController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final AuthService authService;
    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final OtpService otpService;

    @PostMapping("/register")
    /*
     * This method is used to register new data in the system.
     * It validates the request first, then prepares the object,
     * saves it, and returns the final result.
     */
    public User register(@RequestBody RegisterRequest request) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        return authService.register(user);
    }


    @PostMapping("/login")
    /*
     * This method handles login related flow.
     * It checks credentials and then moves user to the next auth step.
     */
    public ApiMessage login(@RequestBody LoginRequest request) {
        authService.login(request);
        return ApiMessage.builder()
                .message("Login OTP sent successfully to registered email")
                .build();
    }

    @PostMapping("/verify-email")
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public AuthResponse verifyEmail(@RequestBody VerifyOtpRequest request) {
        return authService.verifyEmail(request.getEmail(), request.getOtp());
    }

    @PostMapping("/resend-verification-otp")
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public ApiMessage resendVerificationOtp(@RequestBody ForgotPasswordRequest request) {
        authService.resendEmailVerificationOtp(request.getEmail());
        return ApiMessage.builder()
                .message("Verification OTP sent successfully")
                .build();
    }

    @PostMapping("/verify-login-otp")
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public AuthResponse verifyLoginOtp(@RequestBody VerifyOtpRequest request) {
        return authService.verifyLoginOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/forgot-password")
    /*
     * This method is part of the password recovery flow.
     * It helps user regain access to the account in a secure way.
     */
    public ApiMessage forgotPassword(@RequestBody ForgotPasswordRequest request) {
        otpService.generateAndSendOtp(request.getEmail());
        return ApiMessage.builder()
                .message("OTP sent successfully to registered email")
                .build();
    }

    @PostMapping("/verify-otp")
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public ApiMessage verifyOtp(@RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        return ApiMessage.builder()
                .message("OTP verified successfully")
                .build();
    }

    @PostMapping("/reset-password")
    /*
     * This method is part of the password recovery flow.
     * It helps user regain access to the account in a secure way.
     */
    public ApiMessage resetPassword(@RequestBody ResetPasswordRequest request) {
        otpService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ApiMessage.builder()
                .message("Password reset successfully")
                .build();
    }

    @GetMapping("/me")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public User getCurrentUser(Authentication authentication) {
        return authService.getUserByEmail(authentication.getName());
    }

    @GetMapping("/oauth2/success")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public Map<String, String> oauth2Success(@RequestParam("token") String token) {
        return Map.of(
                "message", "Google login successful",
                "token", token
        );
    }


    @GetMapping("/users")
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<User> getAllUsers() {
        return authService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public User getUserById(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @PutMapping("/users/{userId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public User updateProfile(@PathVariable Long userId, @RequestBody User user) {
        return authService.updateProfile(userId, user);
    }

    @PutMapping("/users/{userId}/password")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public User changePassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        return authService.changePassword(userId, request.get("password"));
    }

    @PutMapping("/users/{userId}/deactivate")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public ApiMessage deactivateUser(@PathVariable Long userId) {
        authService.deactivateUser(userId);
        return ApiMessage.builder()
                .message("User deactivated successfully")
                .build();
    }
}
