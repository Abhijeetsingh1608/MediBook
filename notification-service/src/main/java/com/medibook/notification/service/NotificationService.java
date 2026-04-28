package com.medibook.notification.service;

import com.medibook.notification.dto.AppNotificationRequest;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.entity.NotificationRecord;
import java.util.List;

/*
 * This is the service interface for NotificationService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface NotificationService {
    NotificationRecord sendAppNotification(AppNotificationRequest request);

    NotificationRecord sendEmail(EmailNotificationRequest request);

    List<NotificationRecord> getAllNotifications();

    NotificationRecord getNotificationById(Long notificationId, Long loggedInUserId, String role);

    List<NotificationRecord> getNotificationsByUser(Long userId, Long loggedInUserId, String role);

    List<NotificationRecord> getUnreadNotificationsByUser(Long userId, Long loggedInUserId, String role);

    NotificationRecord markAsRead(Long notificationId, Long loggedInUserId, String role);
}
