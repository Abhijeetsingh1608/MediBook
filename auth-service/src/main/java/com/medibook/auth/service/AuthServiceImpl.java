package com.medibook.auth.service;

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
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/*
 * This is the actual implementation class for AuthServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class AuthServiceImpl implements AuthService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final UserRepository userRepository;
    /*
     * This encoder is used to convert sensitive data into secure form before saving.
     */
    private final PasswordEncoder passwordEncoder;
    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final JwtService jwtService;
    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final EmailService emailService;
    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final AuthOtpRepository authOtpRepository;
    /*
     * This helper stores temporary OTP data in Redis for faster lookup.
     * If Redis is not available, service still falls back to database.
     */
    private final RedisOtpStoreService redisOtpStoreService;

    @Value("${app.otp.expiration-minutes}")
    private long otpExpirationMinutes;

    @Value("${app.admin.login.secret}")
    private String adminLoginSecret;

    @Override
    /*
     * This method is used to register new data in the system.
     * It validates the request first, then prepares the object,
     * saves it, and returns the final result.
     */
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Admin cannot be registered publicly");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        user.setActive(true);

        if (user.getProvider() == null || user.getProvider().isBlank()) {
            user.setProvider("LOCAL");
        }

        User savedUser = userRepository.save(user);
        generateAndSendAuthOtp(savedUser, AuthOtpPurpose.EMAIL_VERIFICATION);

        return savedUser;
    }

    @Override
    /*
     * This method handles login related flow.
     * It checks credentials and then moves user to the next auth step.
     */
    public void login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Your account is deactivated. Please contact admin.");
        }

        if (user.getRole() == UserRole.ADMIN) {
            validateAdminSecretKey(request.getAdminSecretKey());
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before login");
        }

        generateAndSendAuthOtp(user, AuthOtpPurpose.LOGIN);
    }

    @Override
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public AuthResponse verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthOtp savedOtp = getLatestAuthOtp(email, AuthOtpPurpose.EMAIL_VERIFICATION);
        validateAuthOtp(savedOtp, otp);

        markAuthOtpAsUsed(savedOtp, AuthOtpPurpose.EMAIL_VERIFICATION);

        user.setEmailVerified(true);
        userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (Exception ex) {
            log.warn("Welcome email failed for {}", user.getEmail(), ex);
        }

        return createAuthResponse(user);
    }

    @Override
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public AuthResponse verifyLoginOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthOtp savedOtp = getLatestAuthOtp(email, AuthOtpPurpose.LOGIN);
        validateAuthOtp(savedOtp, otp);

        markAuthOtpAsUsed(savedOtp, AuthOtpPurpose.LOGIN);

        try {
            emailService.sendLoginAlertEmail(user.getEmail(), user.getFullName());
        } catch (Exception ex) {
            log.warn("Login alert email failed for {}", user.getEmail(), ex);
        }

        return createAuthResponse(user);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void resendEmailVerificationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        generateAndSendAuthOtp(user, AuthOtpPurpose.EMAIL_VERIFICATION);
    }

    @Override
    /*
     * This method handles Google login flow.
     * If user does not already exist, it creates a new account first,
     * then returns the login response for that user.
     */
    public AuthResponse loginWithGoogle(String email, String name) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User googleUser = User.builder()
                    .email(email)
                    .fullName(name)
                    .password(passwordEncoder.encode("google-oauth-user"))
                    .role(UserRole.PATIENT)
                    .provider("GOOGLE")
                    .active(true)
                    .emailVerified(true)
                    .build();

            User savedGoogleUser = userRepository.save(googleUser);

            try {
                emailService.sendWelcomeEmail(savedGoogleUser.getEmail(), savedGoogleUser.getFullName());
            } catch (Exception ex) {
                log.warn("Google signup welcome email failed for {}", savedGoogleUser.getEmail(), ex);
            }

            return savedGoogleUser;
        });

        try {
            emailService.sendLoginAlertEmail(user.getEmail(), user.getFullName());
        } catch (Exception ex) {
            log.warn("Google login alert email failed for {}", user.getEmail(), ex);
        }

        return createAuthResponse(user);
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public User updateProfile(Long userId, User input) {
        User user = getUserById(userId);
        user.setFullName(input.getFullName());
        user.setPhone(input.getPhone());
        user.setProfilePicUrl(input.getProfilePicUrl());
        return userRepository.save(user);
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public User changePassword(Long userId, String password) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public User activateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    public User updateUserRole(Long userId, UserRole role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    private AuthResponse createAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private void generateAndSendAuthOtp(User user, AuthOtpPurpose purpose) {
        String otp = String.format("%06d", new Random().nextInt(1000000));

        AuthOtp authOtp = AuthOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .purpose(purpose)
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();

        boolean storedInRedis = redisOtpStoreService.saveAuthOtp(authOtp);
        if (!storedInRedis) {
            authOtpRepository.save(authOtp);
        }

        log.info("\n=======================================================\n" +
                 "LOCAL DEV OTP: {} OTP for {} is: {}\n" +
                 "=======================================================", purpose, user.getEmail(), otp);

        try {
            if (purpose == AuthOtpPurpose.EMAIL_VERIFICATION) {
                emailService.sendEmailVerificationOtp(user.getEmail(), user.getFullName(), otp);
                return;
            }

            emailService.sendLoginOtpEmail(user.getEmail(), user.getFullName(), otp);
        } catch (Exception ex) {
            log.warn("Failed to send {} OTP email to {} (but continuing for local testing). Error: {}", purpose, user.getEmail(), ex.getMessage());
            // throw new RuntimeException("Unable to send OTP email right now. Please try again in a moment.");
        }
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private AuthOtp getLatestAuthOtp(String email, AuthOtpPurpose purpose) {
        return redisOtpStoreService.getAuthOtp(email, purpose)
                .orElseGet(() -> authOtpRepository.findTopByEmailAndPurposeOrderByIdDesc(email, purpose)
                        .orElseThrow(() -> new RuntimeException("OTP not found")));
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateAuthOtp(AuthOtp savedOtp, String otp) {
        if (savedOtp.isUsed()) {
            throw new RuntimeException("OTP already used");
        }

        if (savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!savedOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    private void validateAdminSecretKey(String adminSecretKey) {
        if (adminSecretKey == null || adminSecretKey.isBlank()) {
            throw new RuntimeException("Admin secret key is required");
        }

        if (!adminLoginSecret.equals(adminSecretKey)) {
            throw new RuntimeException("Invalid admin secret key");
        }
    }

    /*
     * This helper marks an OTP as used after successful verification.
     * Redis entries are deleted because they are no longer needed.
     */
    private void markAuthOtpAsUsed(AuthOtp authOtp, AuthOtpPurpose purpose) {
        if (!redisOtpStoreService.deleteAuthOtp(authOtp.getEmail(), purpose)) {
            authOtp.setUsed(true);
            authOtpRepository.save(authOtp);
        }
    }

}
