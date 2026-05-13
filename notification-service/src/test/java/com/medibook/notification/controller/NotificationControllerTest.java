package com.medibook.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.notification.dto.AppNotificationRequest;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.entity.NotificationChannel;
import com.medibook.notification.entity.NotificationRecord;
import com.medibook.notification.entity.NotificationStatus;
import com.medibook.notification.exception.GlobalExceptionHandler;
import com.medibook.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void sendAppNotification_asAdmin_returnsCreatedRecord() throws Exception {
        NotificationRecord record = sampleRecord();
        when(notificationService.sendAppNotification(any(AppNotificationRequest.class))).thenReturn(record);

        mockMvc.perform(post("/api/v1/notifications/app")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(AppNotificationRequest.builder()
                                .userId(1L)
                                .title("Hello")
                                .message("World")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1));
    }

    @Test
    void sendEmail_withoutAdminRole_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/email")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(EmailNotificationRequest.builder()
                                .userId(1L)
                                .recipientEmail("user@example.com")
                                .subject("Hello")
                                .message("World")
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only admin can send manual notifications"));
    }

    @Test
    void getAllNotifications_asAdmin_returnsList() throws Exception {
        when(notificationService.getAllNotifications()).thenReturn(List.of(sampleRecord()));

        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1));
    }

    @Test
    void getAllNotifications_asNonAdmin_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only admin can view all notifications"));
    }

    @Test
    void getNotificationById_usesCallerHeaders() throws Exception {
        when(notificationService.getNotificationById(9L, 5L, "PATIENT")).thenReturn(sampleRecord());

        mockMvc.perform(get("/api/v1/notifications/9")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1));
    }

    @Test
    void getNotificationsByUser_returnsList() throws Exception {
        when(notificationService.getNotificationsByUser(7L, 5L, "PATIENT")).thenReturn(List.of(sampleRecord()));

        mockMvc.perform(get("/api/v1/notifications/user/7")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1));
    }

    @Test
    void getUnreadNotificationsByUser_returnsList() throws Exception {
        when(notificationService.getUnreadNotificationsByUser(7L, 5L, "PATIENT")).thenReturn(List.of(sampleRecord()));

        mockMvc.perform(get("/api/v1/notifications/user/7/unread")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1));
    }

    @Test
    void markAsRead_returnsUpdatedRecord() throws Exception {
        when(notificationService.markAsRead(8L, 5L, "PATIENT")).thenReturn(sampleRecord());

        mockMvc.perform(post("/api/v1/notifications/8/read")
                        .header("X-User-Id", "5")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1));
    }

    private NotificationRecord sampleRecord() {
        return NotificationRecord.builder()
                .notificationId(1L)
                .userId(5L)
                .subject("Test")
                .message("Message")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .build();
    }
}
