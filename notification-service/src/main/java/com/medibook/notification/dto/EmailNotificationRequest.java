package com.medibook.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
 * This DTO is used to carry request/response data for EmailNotificationRequest.
 * It helps transfer only the required fields between layers.
 */
public class EmailNotificationRequest {

    private Long userId;

    @Email(message = "Recipient email must be valid")
    @NotBlank(message = "Recipient email is required")
    private String recipientEmail;

    private String recipientName;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;
}
