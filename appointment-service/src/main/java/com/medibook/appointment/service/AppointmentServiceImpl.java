package com.medibook.appointment.service;
import com.medibook.appointment.client.ScheduleClient;
import com.medibook.appointment.dto.AppointmentRequest;
import com.medibook.appointment.dto.ScheduleSlotResponse;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.event.AppointmentBookedEvent;
import com.medibook.appointment.event.AppointmentCancelledEvent;
import com.medibook.appointment.repository.AppointmentRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for AppointmentServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class AppointmentServiceImpl implements AppointmentService {

    private static final String SLOT_AVAILABLE = "AVAILABLE";
    private static final String SLOT_BOOKED = "BOOKED";

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final AppointmentRepository appointmentRepository;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final ScheduleClient scheduleClient;
    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final AppointmentEventPublisher appointmentEventPublisher;

    @Override
    /*
     * This method is used to book the selected appointment slot.
     * It checks slot validity first and then saves the appointment.
     */
    public Appointment bookAppointment(AppointmentRequest request, Long patientUserId) {
        validateBookingRequest(request);

        if (appointmentRepository.existsBySlotIdAndStatus(request.getSlotId(), AppointmentStatus.BOOKED)) {
            throw new RuntimeException("This slot is already booked");
        }

        ScheduleSlotResponse slot = scheduleClient.getSlotById(request.getSlotId());
        if (slot == null) {
            throw new RuntimeException("Slot not found");
        }

        if (!request.getProviderId().equals(slot.getProviderId())) {
            throw new RuntimeException("Slot does not belong to selected provider");
        }

        if (!SLOT_AVAILABLE.equalsIgnoreCase(slot.getStatus())) {
            throw new RuntimeException("Slot is not available");
        }

        scheduleClient.updateSlotStatus(request.getSlotId(), SLOT_BOOKED);

        Appointment appointment = Appointment.builder()
                .patientUserId(patientUserId)
                .providerId(request.getProviderId())
                .slotId(request.getSlotId())
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.BOOKED)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        appointmentEventPublisher.publishAppointmentBooked(
                AppointmentBookedEvent.builder()
                        .appointmentId(savedAppointment.getAppointmentId())
                        .patientUserId(savedAppointment.getPatientUserId())
                        .providerId(savedAppointment.getProviderId())
                        .appointmentDate(savedAppointment.getAppointmentDate())
                        .startTime(savedAppointment.getStartTime())
                        .endTime(savedAppointment.getEndTime())
                        .reason(savedAppointment.getReason())
                        .build());

        return savedAppointment;
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByPatient(Long patientUserId) {
        return appointmentRepository.findByPatientUserId(patientUserId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByProvider(Long providerId) {
        return appointmentRepository.findByProviderId(providerId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Override
    /*
     * This method is used internally to collect all active appointments
     * for a particular date. Notification service uses this data
     * to send reminder emails on the day of the appointment.
     */
    public List<Appointment> getBookedAppointmentsForDate(LocalDate appointmentDate) {
        return appointmentRepository.findByAppointmentDateAndStatus(appointmentDate, AppointmentStatus.BOOKED);
    }

    @Override
    /*
     * This method cancels the existing record or appointment.
     * It is used when the current action should no longer stay active.
     */
    public Appointment cancelAppointment(Long appointmentId, Long userId, String role) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (!"ADMIN".equalsIgnoreCase(role) && !appointment.getPatientUserId().equals(userId)) {
            throw new RuntimeException("You are not allowed to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Completed appointment cannot be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        scheduleClient.updateSlotStatus(appointment.getSlotId(), SLOT_AVAILABLE);
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        appointmentEventPublisher.publishAppointmentCancelled(
                AppointmentCancelledEvent.builder()
                        .appointmentId(cancelledAppointment.getAppointmentId())
                        .patientUserId(cancelledAppointment.getPatientUserId())
                        .providerId(cancelledAppointment.getProviderId())
                        .appointmentDate(cancelledAppointment.getAppointmentDate())
                        .startTime(cancelledAppointment.getStartTime())
                        .endTime(cancelledAppointment.getEndTime())
                        .cancelledByRole(role)
                        .build());
        return cancelledAppointment;
    }

    @Override
    /*
     * This method marks the process as completed.
     * It is usually called after the full workflow finishes successfully.
     */
    public Appointment completeAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cancelled appointment cannot be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @Override
    /*
     * This method is used internally when payment order creation fails.
     * It quietly frees the slot so the patient is not left with a stuck booking.
     */
    public Appointment releaseFailedPaymentBooking(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return appointment;
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        scheduleClient.updateSlotStatus(appointment.getSlotId(), SLOT_AVAILABLE);
        return appointmentRepository.save(appointment);
    }

    @Override
    /*
     * This method is used internally after successful payment verification.
     * It makes sure a paid appointment ends up active even if the earlier checkout flow cancelled it first.
     */
    public Appointment activatePaidAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return appointment;
        }

        appointment.setStatus(AppointmentStatus.BOOKED);
        scheduleClient.updateSlotStatus(appointment.getSlotId(), SLOT_BOOKED);
        return appointmentRepository.save(appointment);
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateBookingRequest(AppointmentRequest request) {
        if (request.getProviderId() == null) {
            throw new RuntimeException("Provider id is required");
        }
        if (request.getSlotId() == null) {
            throw new RuntimeException("Slot id is required");
        }
    }
}
