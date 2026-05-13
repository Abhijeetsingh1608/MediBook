package com.medibook.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.notification.client.AppointmentClient;
import com.medibook.notification.client.AuthClient;
import com.medibook.notification.client.ProviderClient;
import com.medibook.notification.dto.AppointmentSummary;
import com.medibook.notification.dto.ProviderSummary;
import com.medibook.notification.dto.UserSummary;
import com.medibook.notification.entity.NotificationChannel;
import com.medibook.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

    @Mock
    private AppointmentClient appointmentClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private ProviderClient providerClient;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AppointmentReminderScheduler scheduler;

    @Test
    void sendTodayAppointmentReminders_skipsPastAppointments() {
        AppointmentSummary appointment = AppointmentSummary.builder()
                .appointmentId(1L)
                .startTime(LocalTime.now().minusMinutes(5))
                .build();
        when(appointmentClient.getBookedAppointmentsForDate(any(LocalDate.class))).thenReturn(List.of(appointment));

        scheduler.sendTodayAppointmentReminders();

        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    void sendTodayAppointmentReminders_skipsAlreadySentReminder() {
        AppointmentSummary appointment = AppointmentSummary.builder()
                .appointmentId(2L)
                .patientUserId(11L)
                .providerId(12L)
                .startTime(LocalTime.now().plusHours(2))
                .build();
        when(appointmentClient.getBookedAppointmentsForDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(notificationRepository.existsByUserIdAndChannelAndSubject(
                11L,
                NotificationChannel.EMAIL,
                "Appointment reminder for today - #2"))
                .thenReturn(true);

        scheduler.sendTodayAppointmentReminders();

        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    void sendTodayAppointmentReminders_sendsReminderForUpcomingAppointment() {
        AppointmentSummary appointment = AppointmentSummary.builder()
                .appointmentId(3L)
                .patientUserId(21L)
                .providerId(31L)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.now().plusHours(1))
                .endTime(LocalTime.now().plusHours(2))
                .reason("Checkup")
                .build();
        when(appointmentClient.getBookedAppointmentsForDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(notificationRepository.existsByUserIdAndChannelAndSubject(
                21L,
                NotificationChannel.EMAIL,
                "Appointment reminder for today - #3"))
                .thenReturn(false);
        when(authClient.getUserById(21L)).thenReturn(UserSummary.builder()
                .userId(21L)
                .fullName("Patient One")
                .email("patient@example.com")
                .build());
        when(providerClient.getProviderById(31L)).thenReturn(ProviderSummary.builder()
                .providerId(31L)
                .fullName("Dr. Demo")
                .build());

        scheduler.sendTodayAppointmentReminders();

        verify(notificationService).sendEmail(any());
    }

    @Test
    void sendTodayAppointmentReminders_handlesReminderFailureGracefully() {
        AppointmentSummary appointment = AppointmentSummary.builder()
                .appointmentId(4L)
                .patientUserId(41L)
                .providerId(51L)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.now().plusHours(1))
                .endTime(LocalTime.now().plusHours(2))
                .build();
        when(appointmentClient.getBookedAppointmentsForDate(any(LocalDate.class))).thenReturn(List.of(appointment));
        when(notificationRepository.existsByUserIdAndChannelAndSubject(
                41L,
                NotificationChannel.EMAIL,
                "Appointment reminder for today - #4"))
                .thenReturn(false);
        when(authClient.getUserById(41L)).thenReturn(UserSummary.builder()
                .userId(41L)
                .email("patient@example.com")
                .build());
        when(providerClient.getProviderById(51L)).thenReturn(null);
        org.mockito.Mockito.doThrow(new RuntimeException("mail failed"))
                .when(notificationService)
                .sendEmail(any());

        scheduler.sendTodayAppointmentReminders();

        verify(notificationService).sendEmail(any());
    }
}
