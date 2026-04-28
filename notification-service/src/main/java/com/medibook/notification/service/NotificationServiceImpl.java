package com.medibook.notification.service;

import com.medibook.notification.dto.AppNotificationRequest;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.entity.NotificationChannel;
import com.medibook.notification.entity.NotificationRecord;
import com.medibook.notification.entity.NotificationStatus;
import com.medibook.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for NotificationServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class NotificationServiceImpl implements NotificationService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final NotificationRepository notificationRepository;
    /*
     * This dependency is required for the working of this class.
     */
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    /*
     * This method saves a dashboard bell notification.
     * It does not send email. It only creates an APP notification record
     * so user can see it inside the application notification panel.
     */
    public NotificationRecord sendAppNotification(AppNotificationRequest request) {
        NotificationRecord notification = NotificationRecord.builder()
                .userId(request.getUserId())
                .recipientName(request.getRecipientName())
                .subject(request.getTitle())
                .message(request.getMessage())
                .channel(NotificationChannel.APP)
                .status(NotificationStatus.SENT)
                .readStatus(false)
                .sentAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public NotificationRecord sendEmail(EmailNotificationRequest request) {
        NotificationRecord notification = NotificationRecord.builder()
                .userId(request.getUserId())
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .subject(request.getSubject())
                .message(request.getMessage())
                .channel(NotificationChannel.EMAIL)
                .readStatus(false)
                .build();

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromAddress);
            mailMessage.setTo(request.getRecipientEmail());
            mailMessage.setSubject(request.getSubject());
            mailMessage.setText(request.getMessage());
            mailSender.send(mailMessage);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
        }

        return notificationRepository.save(notification);
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<NotificationRecord> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public NotificationRecord getNotificationById(Long notificationId, Long loggedInUserId, String role) {
        NotificationRecord notification = findById(notificationId);
        validateOwnerOrAdmin(notification.getUserId(), loggedInUserId, role);
        return notification;
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<NotificationRecord> getNotificationsByUser(Long userId, Long loggedInUserId, String role) {
        validateOwnerOrAdmin(userId, loggedInUserId, role);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<NotificationRecord> getUnreadNotificationsByUser(Long userId, Long loggedInUserId, String role) {
        validateOwnerOrAdmin(userId, loggedInUserId, role);
        return notificationRepository.findByUserIdAndReadStatusFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public NotificationRecord markAsRead(Long notificationId, Long loggedInUserId, String role) {
        NotificationRecord notification = findById(notificationId);
        validateOwnerOrAdmin(notification.getUserId(), loggedInUserId, role);

        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    private NotificationRecord findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateOwnerOrAdmin(Long ownerUserId, Long loggedInUserId, String role) {
        if (ownerUserId == null || "ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        if (!ownerUserId.equals(loggedInUserId)) {
            throw new RuntimeException("You can access only your own notifications");
        }
    }
}
