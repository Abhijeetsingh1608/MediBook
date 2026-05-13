package com.medibook.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.notification.client.AuthClient;
import com.medibook.notification.client.ProviderClient;
import com.medibook.notification.dto.EmailNotificationRequest;
import com.medibook.notification.dto.ProviderSummary;
import com.medibook.notification.dto.UserSummary;
import com.medibook.notification.event.AppointmentBookedEvent;
import com.medibook.notification.event.AppointmentCancelledEvent;
import com.medibook.notification.event.PaymentFailedEvent;
import com.medibook.notification.event.ProviderApprovalRequestedEvent;
import com.medibook.notification.event.ProviderApprovedEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthClient authClient;

    @Mock
    private ProviderClient providerClient;

    @InjectMocks
    private NotificationEventListener eventListener;

    @Test
    @DisplayName("handleAppointmentBooked: sends email when patient found")
    void handleAppointmentBooked_success() {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder()
                .patientUserId(1L)
                .providerId(2L)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();

        UserSummary patient = UserSummary.builder()
                .userId(1L)
                .email("patient@example.com")
                .fullName("John Doe")
                .build();
        ProviderSummary provider = ProviderSummary.builder()
                .providerId(2L)
                .fullName("Dr. Smith")
                .build();

        when(authClient.getUserById(1L)).thenReturn(patient);
        when(providerClient.getProviderById(2L)).thenReturn(provider);

        eventListener.handleAppointmentBooked(event);

        verify(notificationService, times(1)).sendEmail(any(EmailNotificationRequest.class));
    }

    @Test
    @DisplayName("handleAppointmentBooked: skips when patient email missing")
    void handleAppointmentBooked_noEmail_skips() {
        AppointmentBookedEvent event = AppointmentBookedEvent.builder().patientUserId(1L).build();
        UserSummary patient = UserSummary.builder().userId(1L).email("").build();

        when(authClient.getUserById(1L)).thenReturn(patient);

        eventListener.handleAppointmentBooked(event);

        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    @DisplayName("handleAppointmentCancelled: sends email")
    void handleAppointmentCancelled_success() {
        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                .patientUserId(1L)
                .providerId(2L)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .cancelledByRole("PROVIDER")
                .build();

        UserSummary patient = UserSummary.builder()
                .userId(1L)
                .email("patient@example.com")
                .fullName("John Doe")
                .build();

        when(authClient.getUserById(1L)).thenReturn(patient);
        when(providerClient.getProviderById(2L)).thenReturn(null);

        eventListener.handleAppointmentCancelled(event);

        verify(notificationService, times(1)).sendEmail(any(EmailNotificationRequest.class));
    }

    @Test
    void handlePaymentFailed_sendsEmailWithFallbackValues() {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .patientUserId(1L)
                .providerId(2L)
                .amount(BigDecimal.TEN)
                .build();
        UserSummary patient = UserSummary.builder()
                .userId(1L)
                .email("patient@example.com")
                .fullName("")
                .build();

        when(authClient.getUserById(1L)).thenReturn(patient);
        when(providerClient.getProviderById(2L)).thenReturn(null);

        eventListener.handlePaymentFailed(event);

        verify(notificationService).sendEmail(any(EmailNotificationRequest.class));
    }

    @Test
    void handleProviderApprovalRequested_sendsEmailToAdminsWithValidEmailsOnly() {
        ProviderApprovalRequestedEvent event = ProviderApprovalRequestedEvent.builder()
                .providerId(5L)
                .fullName("Dr. Demo")
                .specialization("Dermatology")
                .build();
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of(
                UserSummary.builder().userId(1L).email("admin@example.com").fullName("Admin One").build(),
                UserSummary.builder().userId(2L).email("").fullName("Admin Two").build()));

        eventListener.handleProviderApprovalRequested(event);

        verify(notificationService, times(1)).sendEmail(any(EmailNotificationRequest.class));
    }

    @Test
    void handleProviderApprovalRequested_skipsWhenNoAdmins() {
        when(authClient.getUsersByRole("ADMIN")).thenReturn(List.of());

        eventListener.handleProviderApprovalRequested(ProviderApprovalRequestedEvent.builder().build());

        verify(notificationService, never()).sendEmail(any());
    }

    @Test
    void handleProviderApproved_sendsEmail() {
        ProviderApprovedEvent event = ProviderApprovedEvent.builder()
                .userId(9L)
                .fullName("Dr. Demo")
                .specialization("Neurology")
                .build();
        when(authClient.getUserById(9L)).thenReturn(UserSummary.builder()
                .userId(9L)
                .email("provider@example.com")
                .fullName("Dr. Demo")
                .build());

        eventListener.handleProviderApproved(event);

        verify(notificationService).sendEmail(any(EmailNotificationRequest.class));
    }

    @Test
    void handleProviderApproved_skipsWhenUserMissingEmail() {
        when(authClient.getUserById(9L)).thenReturn(UserSummary.builder().userId(9L).email(" ").build());

        eventListener.handleProviderApproved(ProviderApprovedEvent.builder().userId(9L).build());

        verify(notificationService, never()).sendEmail(any());
    }
}
