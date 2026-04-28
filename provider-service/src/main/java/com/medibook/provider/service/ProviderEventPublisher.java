package com.medibook.provider.service;

import com.medibook.provider.config.RabbitMessagingConfig;
import com.medibook.provider.event.ProviderApprovalRequestedEvent;
import com.medibook.provider.event.ProviderApprovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProviderEventPublisher {

    private static final String PROVIDER_APPROVAL_REQUESTED_KEY = "provider.approval.requested";
    private static final String PROVIDER_APPROVED_KEY = "provider.approved";

    private final RabbitTemplate rabbitTemplate;

    public void publishProviderApprovalRequested(ProviderApprovalRequestedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.NOTIFICATION_EXCHANGE,
                PROVIDER_APPROVAL_REQUESTED_KEY,
                event);
    }

    public void publishProviderApproved(ProviderApprovedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.NOTIFICATION_EXCHANGE,
                PROVIDER_APPROVED_KEY,
                event);
    }
}
