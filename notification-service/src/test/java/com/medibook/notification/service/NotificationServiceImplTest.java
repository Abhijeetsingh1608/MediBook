package com.medibook.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.medibook.notification.dto.AppNotificationRequest;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.entity.NotificationRecord;
import com.medibook.notification.entity.NotificationStatus;
import com.medibook.notification.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "fromAddress", "admin@medibook.com");
    }

    @Test
    @DisplayName("sendAppNotification: saves sent app notification")
    void sendAppNotification_success() {
        AppNotificationRequest request = AppNotificationRequest.builder()
                .userId(1L)
                .recipientName("Aditya")
                .title("Slot Booked")
                .message("Your slot is booked")
                .build();

        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationRecord result = notificationService.sendAppNotification(request);

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.getSubject()).isEqualTo("Slot Booked");
    }

    @Test
    @DisplayName("sendEmail: marks notification sent when mail goes successfully")
    void sendEmail_success() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .userId(1L)
                .recipientEmail("user@medibook.com")
                .recipientName("User")
                .subject("Appointment booked")
                .message("Your appointment is confirmed")
                .build();

        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationRecord result = notificationService.sendEmail(request);

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("sendEmail: marks notification failed when mail sender throws exception")
    void sendEmail_failure() {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .userId(1L)
                .recipientEmail("user@medibook.com")
                .recipientName("User")
                .subject("Appointment booked")
                .message("Your appointment is confirmed")
                .build();

        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        when(notificationRepository.save(any(NotificationRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationRecord result = notificationService.sendEmail(request);

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.getFailureReason()).contains("SMTP error");
    }

    @Test
    @DisplayName("markAsRead: throws when non-owner tries to mark another notification")
    void markAsRead_unauthorized_throwsException() {
        NotificationRecord record = NotificationRecord.builder()
                .notificationId(1L)
                .userId(10L)
                .readStatus(false)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("own notifications");
    }

    @Test
    void getAllNotifications_success() {
        when(notificationRepository.findAll()).thenReturn(java.util.List.of(new NotificationRecord()));
        java.util.List<NotificationRecord> result = notificationService.getAllNotifications();
        assertThat(result).hasSize(1);
    }

    @Test
    void getNotificationsByUser_success() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(java.util.List.of(new NotificationRecord()));
        java.util.List<NotificationRecord> result = notificationService.getNotificationsByUser(1L, 1L, "PATIENT");
        assertThat(result).hasSize(1);
    }

    @Test
    void markAsRead_success() {
        NotificationRecord record = NotificationRecord.builder()
                .notificationId(1L)
                .userId(1L)
                .readStatus(false)
                .build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(record));
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        NotificationRecord result = notificationService.markAsRead(1L, 1L, "PATIENT");

        assertThat(result.isReadStatus()).isTrue();
        assertThat(result.getReadAt()).isNotNull();
    }
}
