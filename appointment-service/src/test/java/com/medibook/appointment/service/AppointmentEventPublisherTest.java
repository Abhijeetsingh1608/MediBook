package com.medibook.appointment.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.medibook.appointment.event.AppointmentBookedEvent;
import com.medibook.appointment.event.AppointmentCancelledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class AppointmentEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AppointmentEventPublisher publisher;

    @Test
    void publishAppointmentBooked_success() {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder().appointmentId(1L).build();
        publisher.publishAppointmentBooked(event);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("appointment.booked"), (Object) eq(event));
    }

    @Test
    void publishAppointmentBooked_failure_logsWarning() {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder().appointmentId(1L).build();
        doThrow(new RuntimeException("Rabbit error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());
        publisher.publishAppointmentBooked(event);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("appointment.booked"), (Object) eq(event));
    }

    @Test
    void publishAppointmentCancelled_success() {
        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder().appointmentId(1L).build();
        publisher.publishAppointmentCancelled(event);
        verify(rabbitTemplate).convertAndSend(anyString(), eq("appointment.cancelled"), (Object) eq(event));
    }
}
