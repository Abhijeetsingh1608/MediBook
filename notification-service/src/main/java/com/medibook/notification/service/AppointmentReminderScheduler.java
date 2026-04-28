package com.medibook.notification.service;

import com.medibook.notification.client.AppointmentClient;
import com.medibook.notification.client.AuthClient;
import com.medibook.notification.client.ProviderClient;
import com.medibook.notification.dto.AppointmentSummary;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.dto.ProviderSummary;
import com.medibook.notification.dto.UserSummary;
import com.medibook.notification.entity.NotificationChannel;
import com.medibook.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
/*
 * This scheduler checks today's booked appointments
 * and sends one reminder mail to each patient.
 * It helps the system behave more like a real application.
 */
public class AppointmentReminderScheduler {

    private final AppointmentClient appointmentClient;
    private final AuthClient authClient;
    private final ProviderClient providerClient;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 8-20 * * *", zone = "Asia/Kolkata")
    /*
     * This scheduled method runs every hour from morning to evening.
     * It sends same-day reminder emails only once for each appointment.
     */
    public void sendTodayAppointmentReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<AppointmentSummary> appointments = appointmentClient.getBookedAppointmentsForDate(today);

        for (AppointmentSummary appointment : appointments) {
            if (appointment.getStartTime() == null || !appointment.getStartTime().isAfter(now)) {
                continue;
            }

            String subject = buildReminderSubject(appointment.getAppointmentId());
            if (notificationRepository.existsByUserIdAndChannelAndSubject(
                    appointment.getPatientUserId(),
                    NotificationChannel.EMAIL,
                    subject)) {
                continue;
            }

            try {
                sendReminder(appointment, subject);
            } catch (Exception ex) {
                log.warn("Failed to send reminder for appointment {}", appointment.getAppointmentId(), ex);
            }
        }
    }

    private void sendReminder(AppointmentSummary appointment, String subject) {
        UserSummary patient = authClient.getUserById(appointment.getPatientUserId());
        ProviderSummary provider = providerClient.getProviderById(appointment.getProviderId());
        if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
            return;
        }

        String providerName = provider == null ? "your selected provider" : safeValue(provider.getFullName(), "your selected provider");
        String message = String.format(
                "Hello %s,%n%nThis is a reminder that you have an appointment scheduled for today.%n%nProvider: %s%nDate: %s%nTime: %s to %s%nReason: %s%n%nPlease be ready a little before your slot time.%n%nThank you,%nMediBook",
                safeValue(patient.getFullName(), "User"),
                providerName,
                safeValue(appointment.getAppointmentDate() == null ? null : appointment.getAppointmentDate().toString(), "Today"),
                safeValue(appointment.getStartTime() == null ? null : appointment.getStartTime().toString(), "Not provided"),
                safeValue(appointment.getEndTime() == null ? null : appointment.getEndTime().toString(), "Not provided"),
                safeValue(appointment.getReason(), "General consultation"));

        notificationService.sendEmail(EmailNotificationRequest.builder()
                .userId(patient.getUserId())
                .recipientEmail(patient.getEmail())
                .recipientName(patient.getFullName())
                .subject(subject)
                .message(message)
                .build());
    }

    private String buildReminderSubject(Long appointmentId) {
        return "Appointment reminder for today - #" + appointmentId;
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
