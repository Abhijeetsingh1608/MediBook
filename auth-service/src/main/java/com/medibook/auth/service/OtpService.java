package com.medibook.auth.service;

/*
 * This is the service interface for OtpService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface OtpService {

    void generateAndSendOtp(String email);

    void verifyOtp(String email, String otp);

    void resetPassword(String email, String otp, String newPassword);
}
