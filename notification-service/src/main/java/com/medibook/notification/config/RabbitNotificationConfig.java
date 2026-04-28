package com.medibook.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificationConfig {

    public static final String NOTIFICATION_EXCHANGE = "medibook.notification.exchange";
    public static final String APPOINTMENT_BOOKED_QUEUE = "medibook.notification.appointment.booked";
    public static final String APPOINTMENT_CANCELLED_QUEUE = "medibook.notification.appointment.cancelled";
    public static final String PAYMENT_FAILED_QUEUE = "medibook.notification.payment.failed";
    public static final String PROVIDER_APPROVAL_REQUESTED_QUEUE = "medibook.notification.provider.approval.requested";
    public static final String PROVIDER_APPROVED_QUEUE = "medibook.notification.provider.approved";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue appointmentBookedQueue() {
        return new Queue(APPOINTMENT_BOOKED_QUEUE);
    }

    @Bean
    public Queue appointmentCancelledQueue() {
        return new Queue(APPOINTMENT_CANCELLED_QUEUE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE);
    }

    @Bean
    public Queue providerApprovalRequestedQueue() {
        return new Queue(PROVIDER_APPROVAL_REQUESTED_QUEUE);
    }

    @Bean
    public Queue providerApprovedQueue() {
        return new Queue(PROVIDER_APPROVED_QUEUE);
    }

    @Bean
    public Binding appointmentBookedBinding(Queue appointmentBookedQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(appointmentBookedQueue)
                .to(notificationExchange)
                .with("appointment.booked");
    }

    @Bean
    public Binding appointmentCancelledBinding(Queue appointmentCancelledQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(appointmentCancelledQueue)
                .to(notificationExchange)
                .with("appointment.cancelled");
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(paymentFailedQueue)
                .to(notificationExchange)
                .with("payment.failed");
    }

    @Bean
    public Binding providerApprovalRequestedBinding(
            Queue providerApprovalRequestedQueue,
            DirectExchange notificationExchange) {
        return BindingBuilder.bind(providerApprovalRequestedQueue)
                .to(notificationExchange)
                .with("provider.approval.requested");
    }

    @Bean
    public Binding providerApprovedBinding(Queue providerApprovedQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(providerApprovedQueue)
                .to(notificationExchange)
                .with("provider.approved");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
