package com.medibook.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderApprovalRequestedEvent {
    private Long providerId;
    private Long userId;
    private String fullName;
    private String specialization;
    private String qualification;
    private String clinicName;
    private String clinicAddress;
    private String bio;
}
