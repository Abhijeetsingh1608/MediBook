package com.medibook.auth.service;

import com.medibook.auth.entity.PasswordResetOtp;
import com.medibook.auth.entity.User;
import com.medibook.auth.repository.PasswordResetOtpRepository;
import com.medibook.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 * This is the actual implementation class for OtpServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class OtpServiceImpl implements OtpService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final PasswordResetOtpRepository otpRepository;
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
    private final EmailService emailService;
    /*
     * This helper stores temporary OTP data in Redis for faster access.
     * If Redis is not available, service falls back to database.
     */
    private final RedisOtpStoreService redisOtpStoreService;

    @Value("${app.otp.expiration-minutes}")
    private long otpExpirationMinutes;

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String otp = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .verified(false)
                .used(false)
                .build();

        boolean storedInRedis = redisOtpStoreService.savePasswordResetOtp(passwordResetOtp);
        if (!storedInRedis) {
            otpRepository.save(passwordResetOtp);
        }

        try {
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
        } catch (Exception ex) {
            log.warn("Password reset OTP email failed for {}", user.getEmail(), ex);
        }
    }

    @Override
    /*
     * This method verifies the given value before allowing the next step.
     * It is important for security, OTP flow, or payment validation.
     */
    public void verifyOtp(String email, String otp) {
        PasswordResetOtp savedOtp = getLatestPasswordResetOtp(email);

        validateOtp(savedOtp, otp);
        savedOtp.setVerified(true);
        if (!redisOtpStoreService.updatePasswordResetOtp(savedOtp)) {
            otpRepository.save(savedOtp);
        }
    }

    @Override
    /*
     * This method is part of the password recovery flow.
     * It helps user regain access to the account in a secure way.
     */
    public void resetPassword(String email, String otp, String newPassword) {
        PasswordResetOtp savedOtp = getLatestPasswordResetOtp(email);

        validateOtp(savedOtp, otp);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        savedOtp.setVerified(true);
        savedOtp.setUsed(true);
        if (!redisOtpStoreService.deletePasswordResetOtp(email)) {
            otpRepository.save(savedOtp);
        }
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateOtp(PasswordResetOtp savedOtp, String otp) {
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

    /*
     * This helper fetches latest forgot-password OTP.
     * Redis is checked first, then database fallback is used.
     */
    private PasswordResetOtp getLatestPasswordResetOtp(String email) {
        return redisOtpStoreService.getPasswordResetOtp(email)
                .orElseGet(() -> otpRepository.findTopByEmailOrderByIdDesc(email)
                        .orElseThrow(() -> new RuntimeException("OTP not found")));
    }
}
