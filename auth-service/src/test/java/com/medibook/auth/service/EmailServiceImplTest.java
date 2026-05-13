package com.medibook.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendOtpEmail_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendOtpEmail("test@example.com", "Test User", "123456");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendWelcomeEmail_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendWelcomeEmail("test@example.com", "Test User");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailVerificationOtp_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmailVerificationOtp("test@example.com", "Test User", "123456");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
