package com.medibook.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO carries simple in-app notification data.
 * It is used when we want the bell notification inside dashboard,
 * without sending an email to the user.
 */
public class AppNotificationRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    private String recipientName;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;
}
