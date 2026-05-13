package com.medibook.appointment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.appointment.client.ScheduleClient;
import com.medibook.appointment.dto.AppointmentRequest;
import com.medibook.appointment.dto.ScheduleSlotResponse;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.repository.AppointmentRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ScheduleClient scheduleClient;

    @Mock
    private AppointmentEventPublisher appointmentEventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private AppointmentRequest request;
    private ScheduleSlotResponse slotResponse;

    @BeforeEach
    void setUp() {
        request = AppointmentRequest.builder()
                .providerId(3L)
                .slotId(7L)
                .reason("Eye checkup")
                .build();

        slotResponse = new ScheduleSlotResponse(
                7L,
                3L,
                30L,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                "AVAILABLE");
    }

    @Test
    @DisplayName("bookAppointment: success - books slot and publishes event")
    void bookAppointment_success() {
        when(appointmentRepository.existsBySlotIdAndStatus(7L, AppointmentStatus.BOOKED)).thenReturn(false);
        when(scheduleClient.getSlotById(7L)).thenReturn(slotResponse);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setAppointmentId(1L);
            return appointment;
        });

        Appointment result = appointmentService.bookAppointment(request, 9L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
        verify(scheduleClient).updateSlotStatus(7L, "BOOKED");
        verify(appointmentEventPublisher).publishAppointmentBooked(any());
    }

    @Test
    @DisplayName("bookAppointment: throws when slot is already booked")
    void bookAppointment_alreadyBooked_throwsException() {
        when(appointmentRepository.existsBySlotIdAndStatus(7L, AppointmentStatus.BOOKED)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.bookAppointment(request, 9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("cancelAppointment: success - cancels appointment and frees slot")
    void cancelAppointment_success() {
        Appointment appointment = Appointment.builder()
                .appointmentId(1L)
                .patientUserId(9L)
                .providerId(3L)
                .slotId(7L)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.BOOKED)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.cancelAppointment(1L, 9L, "PATIENT");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(scheduleClient).updateSlotStatus(7L, "AVAILABLE");
        verify(appointmentEventPublisher).publishAppointmentCancelled(any());
    }

    @Test
    @DisplayName("cancelAppointment: throws when user tries to cancel someone else's appointment")
    void cancelAppointment_unauthorized_throwsException() {
        Appointment appointment = Appointment.builder()
                .appointmentId(1L)
                .patientUserId(9L)
                .slotId(7L)
                .status(AppointmentStatus.BOOKED)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 99L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    @DisplayName("activatePaidAppointment: restores booked status and slot")
    void activatePaidAppointment_success() {
        Appointment appointment = Appointment.builder()
                .appointmentId(1L)
                .slotId(7L)
                .status(AppointmentStatus.CANCELLED)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.activatePaidAppointment(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.BOOKED);
        verify(scheduleClient).updateSlotStatus(7L, "BOOKED");
    }

    @Test
    void bookAppointment_missingProvider_throwsException() {
        request.setProviderId(null);
        assertThatThrownBy(() -> appointmentService.bookAppointment(request, 9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Provider id is required");
    }

    @Test
    void bookAppointment_missingSlot_throwsException() {
        request.setSlotId(null);
        assertThatThrownBy(() -> appointmentService.bookAppointment(request, 9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot id is required");
    }

    @Test
    void cancelAppointment_alreadyCancelled_throwsException() {
        Appointment appointment = Appointment.builder().status(AppointmentStatus.CANCELLED).patientUserId(9L).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 9L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void completeAppointment_alreadyCancelled_throwsException() {
        Appointment appointment = Appointment.builder().status(AppointmentStatus.CANCELLED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> appointmentService.completeAppointment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cancelled appointment cannot be completed");
    }

    @Test
    void getAllAppointments_success() {
        when(appointmentRepository.findAll()).thenReturn(java.util.List.of(new Appointment()));
        assertThat(appointmentService.getAllAppointments()).hasSize(1);
    }

    @Test
    void getAppointmentsByPatient_success() {
        when(appointmentRepository.findByPatientUserId(9L)).thenReturn(java.util.List.of(new Appointment()));
        assertThat(appointmentService.getAppointmentsByPatient(9L)).hasSize(1);
    }

    @Test
    void releaseFailedPaymentBooking_success() {
        Appointment appointment = Appointment.builder().appointmentId(1L).slotId(7L).status(AppointmentStatus.BOOKED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Appointment result = appointmentService.releaseFailedPaymentBooking(1L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(scheduleClient).updateSlotStatus(7L, "AVAILABLE");
    }

    @Test
    void cancelAppointment_completed_throwsException() {
        Appointment appointment = Appointment.builder().status(AppointmentStatus.COMPLETED).patientUserId(9L).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, 9L, "PATIENT"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Completed appointment cannot be cancelled");
    }

    @Test
    void releaseFailedPaymentBooking_alreadyCancelled_returnsAppointment() {
        Appointment appointment = Appointment.builder().status(AppointmentStatus.CANCELLED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        Appointment result = appointmentService.releaseFailedPaymentBooking(1L);
        assertThat(result).isEqualTo(appointment);
    }

    @Test
    void activatePaidAppointment_alreadyCompleted_returnsAppointment() {
        Appointment appointment = Appointment.builder().status(AppointmentStatus.COMPLETED).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        Appointment result = appointmentService.activatePaidAppointment(1L);
        assertThat(result).isEqualTo(appointment);
    }
}
