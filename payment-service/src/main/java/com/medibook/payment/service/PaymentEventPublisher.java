package com.medibook.payment.service;

import com.medibook.payment.config.RabbitMessagingConfig;
import com.medibook.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private static final String PAYMENT_FAILED_KEY = "payment.failed";

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentFailed(PaymentFailedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMessagingConfig.NOTIFICATION_EXCHANGE,
                PAYMENT_FAILED_KEY,
                event);
    }
}
