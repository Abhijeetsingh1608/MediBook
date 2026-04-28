package com.medibook.auth.service;

/*
 * This is the service interface for EmailService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface EmailService {

    void sendWelcomeEmail(String toEmail, String fullName);

    void sendLoginAlertEmail(String toEmail, String fullName);

    void sendOtpEmail(String toEmail, String fullName, String otp);

    void sendEmailVerificationOtp(String toEmail, String fullName, String otp);

    void sendLoginOtpEmail(String toEmail, String fullName, String otp);
}
