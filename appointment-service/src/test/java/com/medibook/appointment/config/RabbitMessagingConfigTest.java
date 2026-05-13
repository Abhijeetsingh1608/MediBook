package com.medibook.appointment.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

class RabbitMessagingConfigTest {

    private final RabbitMessagingConfig config = new RabbitMessagingConfig();

    @Test
    void notificationExchange_exists() {
        org.springframework.amqp.core.DirectExchange exchange = config.notificationExchange();
        assertThat(exchange.getName()).isEqualTo(RabbitMessagingConfig.NOTIFICATION_EXCHANGE);
    }

    @Test
    void messageConverter_exists() {
        org.springframework.amqp.support.converter.MessageConverter converter = config.jsonMessageConverter();
        assertThat(converter).isNotNull();
    }

    @Test
    void rabbitTemplate_exists() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        org.springframework.amqp.support.converter.MessageConverter converter = config.jsonMessageConverter();
        RabbitTemplate template = config.rabbitTemplate(connectionFactory, converter);
        assertThat(template).isNotNull();
        assertThat(template.getMessageConverter()).isEqualTo(converter);
    }
}
