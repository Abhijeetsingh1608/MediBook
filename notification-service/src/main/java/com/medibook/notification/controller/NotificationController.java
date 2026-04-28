package com.medibook.notification.controller;

import com.medibook.notification.dto.AppNotificationRequest;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.entity.NotificationRecord;
import com.medibook.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
/*
 * This controller handles API requests for NotificationController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class NotificationController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final NotificationService notificationService;

    @PostMapping("/app")
    /*
     * This method creates an in-app bell notification.
     * It is used when frontend should show notification inside dashboard
     * without sending an email.
     */
    public NotificationRecord sendAppNotification(
            @Valid @RequestBody AppNotificationRequest request,
            HttpServletRequest httpRequest) {

        validateAdminAccess(httpRequest);
        return notificationService.sendAppNotification(request);
    }

    @PostMapping("/email")
    /*
     * This method sends the required OTP, email, or notification.
     * It is used when the system has to inform the user about an action.
     */
    public NotificationRecord sendEmail(
            @Valid @RequestBody EmailNotificationRequest request,
            HttpServletRequest httpRequest) {

        validateAdminAccess(httpRequest);
        return notificationService.sendEmail(request);
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<NotificationRecord> getAllNotifications(HttpServletRequest httpRequest) {
        if (!"ADMIN".equalsIgnoreCase(getRole(httpRequest))) {
            throw new RuntimeException("Only admin can view all notifications");
        }
        return notificationService.getAllNotifications();
    }

    @GetMapping("/{notificationId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public NotificationRecord getNotificationById(
            @PathVariable Long notificationId,
            HttpServletRequest httpRequest) {
        return notificationService.getNotificationById(
                notificationId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @GetMapping("/user/{userId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<NotificationRecord> getNotificationsByUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        return notificationService.getNotificationsByUser(
                userId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @GetMapping("/user/{userId}/unread")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<NotificationRecord> getUnreadNotificationsByUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        return notificationService.getUnreadNotificationsByUser(
                userId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    @PostMapping("/{notificationId}/read")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public NotificationRecord markAsRead(
            @PathVariable Long notificationId,
            HttpServletRequest httpRequest) {
        return notificationService.markAsRead(
                notificationId,
                getUserId(httpRequest),
                getRole(httpRequest));
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        return userId == null ? null : Long.valueOf(userId);
    }

    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    private String getRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }

    /*
     * Only admin should be able to send manual app or email notifications
     * from the frontend notification APIs.
     * System generated emails are sent internally from the service layer,
     * so they do not depend on this controller check.
     */
    private void validateAdminAccess(HttpServletRequest request) {
        String role = getRole(request);
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        throw new RuntimeException("Only admin can send manual notifications");
    }
}
