package com.medibook.appointment.service;

import com.medibook.appointment.config.RabbitMessagingConfig;
import com.medibook.appointment.event.AppointmentBookedEvent;
import com.medibook.appointment.event.AppointmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    private static final String APPOINTMENT_BOOKED_KEY = "appointment.booked";
    private static final String APPOINTMENT_CANCELLED_KEY = "appointment.cancelled";
    private static final Logger log = LoggerFactory.getLogger(AppointmentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishAppointmentBooked(AppointmentBookedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMessagingConfig.NOTIFICATION_EXCHANGE,
                    APPOINTMENT_BOOKED_KEY,
                    event);
        } catch (RuntimeException ex) {
            log.warn("Could not publish appointment booked event for appointment {}", event.getAppointmentId(), ex);
        }
    }

    public void publishAppointmentCancelled(AppointmentCancelledEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMessagingConfig.NOTIFICATION_EXCHANGE,
                    APPOINTMENT_CANCELLED_KEY,
                    event);
        } catch (RuntimeException ex) {
            log.warn("Could not publish appointment cancelled event for appointment {}", event.getAppointmentId(), ex);
        }
    }
}
