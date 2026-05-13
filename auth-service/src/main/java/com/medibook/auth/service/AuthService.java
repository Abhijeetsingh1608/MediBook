package com.medibook.auth.service;

import com.medibook.auth.dto.AuthResponse;
import com.medibook.auth.dto.LoginRequest;
import com.medibook.auth.entity.User;
import com.medibook.auth.entity.UserRole;
import java.util.List;

/*
 * This is the service interface for AuthService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface AuthService {

    User register(User user);

    void login(LoginRequest request);

    AuthResponse verifyEmail(String email, String otp);

    AuthResponse verifyLoginOtp(String email, String otp);

    void resendEmailVerificationOtp(String email);

    AuthResponse loginWithGoogle(String email, String name);

    List<User> getAllUsers();

    List<User> getUsersByRole(UserRole role);

    User getUserById(Long userId);

    User getUserByEmail(String email);

    User updateProfile(Long userId, User user);

    User changePassword(Long userId, String password);

    void deactivateUser(Long userId);

    User activateUser(Long userId);

    User updateUserRole(Long userId, UserRole role);
}
