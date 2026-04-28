package com.medibook.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 * This is the actual implementation class for EmailServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class EmailServiceImpl implements EmailService {

    /*
     * This dependency is required for the working of this class.
     */
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Welcome to MediBook";
        String body = "Hello " + fullName + ",\n\n"
                + "Welcome to MediBook.\n"
                + "Your account has been created successfully.\n\n"
                + "Regards,\n"
                + "MediBook Team";

        sendEmail(toEmail, subject, body);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void sendLoginAlertEmail(String toEmail, String fullName) {
        String subject = "Login Alert - MediBook";
        String body = "Hello " + fullName + ",\n\n"
                + "Your account was logged in successfully.\n\n"
                + "Regards,\n"
                + "MediBook Team";

        sendEmail(toEmail, subject, body);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void sendOtpEmail(String toEmail, String fullName, String otp) {
        String subject = "MediBook Password Reset OTP";
        String body = "Hello " + fullName + ",\n\n"
                + "Your OTP is: " + otp + "\n\n"
                + "Regards,\n"
                + "MediBook Team";

        sendEmail(toEmail, subject, body);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void sendEmailVerificationOtp(String toEmail, String fullName, String otp) {
        String subject = "Verify your MediBook email";
        String body = "Hello " + fullName + ",\n\n"
                + "Your email verification OTP is: " + otp + "\n"
                + "This OTP will expire soon.\n\n"
                + "Regards,\n"
                + "MediBook Team";

        sendEmail(toEmail, subject, body);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public void sendLoginOtpEmail(String toEmail, String fullName, String otp) {
        String subject = "MediBook Login OTP";
        String body = "Hello " + fullName + ",\n\n"
                + "Your login OTP is: " + otp + "\n"
                + "Use this OTP to complete your login.\n\n"
                + "Regards,\n"
                + "MediBook Team";

        sendEmail(toEmail, subject, body);
    }

    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    private void sendEmail(String toEmail, String subject, String body) {
        log.info("Trying to send email to {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Email sent successfully to {}", toEmail);
    }
}
