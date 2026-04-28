package com.medibook.notification.service;

import com.medibook.notification.client.AuthClient;
import com.medibook.notification.client.ProviderClient;
import com.medibook.notification.config.RabbitNotificationConfig;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.dto.ProviderSummary;
import com.medibook.notification.dto.UserSummary;
import com.medibook.notification.event.AppointmentBookedEvent;
import com.medibook.notification.event.AppointmentCancelledEvent;
import com.medibook.notification.event.PaymentFailedEvent;
import com.medibook.notification.event.ProviderApprovalRequestedEvent;
import com.medibook.notification.event.ProviderApprovedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final AuthClient authClient;
    private final ProviderClient providerClient;

    @RabbitListener(queues = RabbitNotificationConfig.APPOINTMENT_BOOKED_QUEUE)
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        UserSummary patient = authClient.getUserById(event.getPatientUserId());
        ProviderSummary provider = providerClient.getProviderById(event.getProviderId());
        if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
            return;
        }

        String providerName = provider == null ? "your selected provider" : provider.getFullName();
        String subject = "Appointment booked successfully";
        String message = String.format(
                "Hello %s,%n%nYour appointment has been booked successfully.%n%nProvider: %s%nDate: %s%nTime: %s to %s%nReason: %s%n%nThank you,%nMediBook",
                safeName(patient.getFullName()),
                providerName,
                event.getAppointmentDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getReason() == null || event.getReason().isBlank() ? "General consultation" : event.getReason());

        notificationService.sendEmail(EmailNotificationRequest.builder()
                .userId(patient.getUserId())
                .recipientEmail(patient.getEmail())
                .recipientName(patient.getFullName())
                .subject(subject)
                .message(message)
                .build());
    }

    @RabbitListener(queues = RabbitNotificationConfig.APPOINTMENT_CANCELLED_QUEUE)
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        UserSummary patient = authClient.getUserById(event.getPatientUserId());
        ProviderSummary provider = providerClient.getProviderById(event.getProviderId());
        if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
            return;
        }

        String providerName = provider == null ? "your selected provider" : provider.getFullName();
        String cancelledBy = event.getCancelledByRole() == null ? "system" : event.getCancelledByRole().toLowerCase();
        String subject = "Appointment cancelled";
        String message = String.format(
                "Hello %s,%n%nYour appointment has been cancelled.%n%nProvider: %s%nDate: %s%nTime: %s to %s%nCancelled by: %s%n%nPlease book another slot if needed.%n%nThank you,%nMediBook",
                safeName(patient.getFullName()),
                providerName,
                event.getAppointmentDate(),
                event.getStartTime(),
                event.getEndTime(),
                cancelledBy);

        notificationService.sendEmail(EmailNotificationRequest.builder()
                .userId(patient.getUserId())
                .recipientEmail(patient.getEmail())
                .recipientName(patient.getFullName())
                .subject(subject)
                .message(message)
                .build());
    }

    @RabbitListener(queues = RabbitNotificationConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        UserSummary patient = authClient.getUserById(event.getPatientUserId());
        ProviderSummary provider = providerClient.getProviderById(event.getProviderId());
        if (patient == null || patient.getEmail() == null || patient.getEmail().isBlank()) {
            return;
        }

        String providerName = provider == null ? "your selected provider" : provider.getFullName();
        String subject = "Payment failed for your appointment";
        String message = String.format(
                "Hello %s,%n%nWe could not complete your appointment payment.%n%nProvider: %s%nDate: %s%nTime: %s to %s%nAmount: %s %s%nReason: %s%n%nIf money was deducted incorrectly, please contact support. You can also try booking the slot again.%n%nThank you,%nMediBook",
                safeName(patient.getFullName()),
                providerName,
                valueOrFallback(event.getAppointmentDate() == null ? null : event.getAppointmentDate().toString()),
                valueOrFallback(event.getStartTime() == null ? null : event.getStartTime().toString()),
                valueOrFallback(event.getEndTime() == null ? null : event.getEndTime().toString()),
                event.getAmount() == null ? "0" : event.getAmount().toPlainString(),
                valueOrFallback(event.getCurrency()),
                valueOrFallback(event.getFailureReason()));

        notificationService.sendEmail(EmailNotificationRequest.builder()
                .userId(patient.getUserId())
                .recipientEmail(patient.getEmail())
                .recipientName(patient.getFullName())
                .subject(subject)
                .message(message)
                .build());
    }

    @RabbitListener(queues = RabbitNotificationConfig.PROVIDER_APPROVAL_REQUESTED_QUEUE)
    public void handleProviderApprovalRequested(ProviderApprovalRequestedEvent event) {
        List<UserSummary> admins = authClient.getUsersByRole("ADMIN");
        if (admins.isEmpty()) {
            return;
        }

        String subject = "New provider approval request";
        String message = String.format(
                "Hello Admin,%n%nA new provider has registered and is waiting for approval.%n%nProvider ID: %s%nName: %s%nSpecialization: %s%nQualification: %s%nClinic: %s%nAddress: %s%nBio: %s%n%nPlease login to MediBook admin panel and review this profile.%n%nThank you,%nMediBook",
                event.getProviderId(),
                event.getFullName(),
                valueOrFallback(event.getSpecialization()),
                valueOrFallback(event.getQualification()),
                valueOrFallback(event.getClinicName()),
                valueOrFallback(event.getClinicAddress()),
                valueOrFallback(event.getBio()));

        for (UserSummary admin : admins) {
            if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                continue;
            }
            notificationService.sendEmail(EmailNotificationRequest.builder()
                    .userId(admin.getUserId())
                    .recipientEmail(admin.getEmail())
                    .recipientName(admin.getFullName())
                    .subject(subject)
                    .message(message)
                    .build());
        }
    }

    @RabbitListener(queues = RabbitNotificationConfig.PROVIDER_APPROVED_QUEUE)
    public void handleProviderApproved(ProviderApprovedEvent event) {
        UserSummary providerUser = authClient.getUserById(event.getUserId());
        if (providerUser == null || providerUser.getEmail() == null || providerUser.getEmail().isBlank()) {
            return;
        }

        String subject = "Provider profile approved";
        String message = String.format(
                "Hello %s,%n%nCongratulations. Your provider profile has been approved by the MediBook admin team.%n%nName: %s%nSpecialization: %s%n%nYou can now login, create slots, and start receiving patients on the platform.%n%nThank you,%nMediBook",
                safeName(providerUser.getFullName()),
                event.getFullName(),
                valueOrFallback(event.getSpecialization()));

        notificationService.sendEmail(EmailNotificationRequest.builder()
                .userId(providerUser.getUserId())
                .recipientEmail(providerUser.getEmail())
                .recipientName(providerUser.getFullName())
                .subject(subject)
                .message(message)
                .build());
    }

    private String safeName(String fullName) {
        return fullName == null || fullName.isBlank() ? "User" : fullName;
    }

    private String valueOrFallback(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }
}
