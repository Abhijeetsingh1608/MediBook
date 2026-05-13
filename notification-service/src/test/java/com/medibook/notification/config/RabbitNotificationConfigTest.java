package com.medibook.notification.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

class RabbitNotificationConfigTest {

    private final RabbitNotificationConfig config = new RabbitNotificationConfig();

    @Test
    void declaresExchangeQueuesAndBindings() {
        DirectExchange exchange = config.notificationExchange();
        Queue booked = config.appointmentBookedQueue();
        Queue cancelled = config.appointmentCancelledQueue();
        Queue failed = config.paymentFailedQueue();
        Queue requested = config.providerApprovalRequestedQueue();
        Queue approved = config.providerApprovedQueue();

        Binding bookedBinding = config.appointmentBookedBinding(booked, exchange);
        Binding cancelledBinding = config.appointmentCancelledBinding(cancelled, exchange);
        Binding failedBinding = config.paymentFailedBinding(failed, exchange);
        Binding requestedBinding = config.providerApprovalRequestedBinding(requested, exchange);
        Binding approvedBinding = config.providerApprovedBinding(approved, exchange);

        assertThat(exchange.getName()).isEqualTo(RabbitNotificationConfig.NOTIFICATION_EXCHANGE);
        assertThat(booked.getName()).isEqualTo(RabbitNotificationConfig.APPOINTMENT_BOOKED_QUEUE);
        assertThat(cancelled.getName()).isEqualTo(RabbitNotificationConfig.APPOINTMENT_CANCELLED_QUEUE);
        assertThat(failed.getName()).isEqualTo(RabbitNotificationConfig.PAYMENT_FAILED_QUEUE);
        assertThat(requested.getName()).isEqualTo(RabbitNotificationConfig.PROVIDER_APPROVAL_REQUESTED_QUEUE);
        assertThat(approved.getName()).isEqualTo(RabbitNotificationConfig.PROVIDER_APPROVED_QUEUE);
        assertThat(bookedBinding.getExchange()).isEqualTo(exchange.getName());
        assertThat(cancelledBinding.getExchange()).isEqualTo(exchange.getName());
        assertThat(failedBinding.getExchange()).isEqualTo(exchange.getName());
        assertThat(requestedBinding.getExchange()).isEqualTo(exchange.getName());
        assertThat(approvedBinding.getExchange()).isEqualTo(exchange.getName());
    }

    @Test
    void createsRabbitBeans() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        MessageConverter messageConverter = config.jsonMessageConverter();

        RabbitTemplate rabbitTemplate = config.rabbitTemplate(connectionFactory, messageConverter);
        SimpleRabbitListenerContainerFactory factory =
                config.rabbitListenerContainerFactory(connectionFactory, messageConverter);

        assertThat(messageConverter).isNotNull();
        assertThat(rabbitTemplate.getMessageConverter()).isEqualTo(messageConverter);
        assertThat(factory).isNotNull();
    }
}
