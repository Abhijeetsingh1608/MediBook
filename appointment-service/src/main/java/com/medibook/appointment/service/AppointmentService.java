package com.medibook.appointment.service;

import com.medibook.appointment.dto.AppointmentRequest;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;

/*
 * This is the service interface for AppointmentService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface AppointmentService {

    Appointment bookAppointment(AppointmentRequest request, Long patientUserId);

    List<Appointment> getAllAppointments();

    Appointment getAppointmentById(Long appointmentId);

    List<Appointment> getAppointmentsByPatient(Long patientUserId);

    List<Appointment> getAppointmentsByProvider(Long providerId);

    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);

    Appointment cancelAppointment(Long appointmentId, Long userId, String role);

    Appointment completeAppointment(Long appointmentId);

    Appointment releaseFailedPaymentBooking(Long appointmentId);

    Appointment activatePaidAppointment(Long appointmentId);

    List<Appointment> getBookedAppointmentsForDate(LocalDate appointmentDate);
}
