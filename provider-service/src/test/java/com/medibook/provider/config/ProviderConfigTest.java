package com.medibook.provider.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import static org.mockito.Mockito.mock;

class ProviderConfigTest {

    @Test
    void rabbitMessagingConfig_beansAreNotNull() {
        RabbitMessagingConfig config = new RabbitMessagingConfig();
        DirectExchange exchange = config.notificationExchange();
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo(RabbitMessagingConfig.NOTIFICATION_EXCHANGE);

        MessageConverter converter = config.jsonMessageConverter();
        assertThat(converter).isNotNull();

        ConnectionFactory factory = mock(ConnectionFactory.class);
        RabbitTemplate template = config.rabbitTemplate(factory, converter);
        assertThat(template).isNotNull();
        assertThat(template.getMessageConverter()).isEqualTo(converter);
    }
}
