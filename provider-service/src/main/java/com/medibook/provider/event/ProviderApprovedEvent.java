package com.medibook.provider.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderApprovedEvent {
    private Long providerId;
    private Long userId;
    private String fullName;
    private String specialization;
}
