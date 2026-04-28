package com.medibook.appointment.controller;

import com.medibook.appointment.dto.AppointmentRequest;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
/*
 * This controller handles API requests for AppointmentController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class AppointmentController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final AppointmentService appointmentService;

    @PostMapping
    @Operation(summary = "Book appointment for logged-in patient")
    /*
     * This method is used to book the selected appointment slot.
     * It checks slot validity first and then saves the appointment.
     */
    public Appointment bookAppointment(@RequestBody AppointmentRequest request, HttpServletRequest httpRequest) {
        Long patientUserId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");

        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only patient users can book appointments");
        }

        return appointmentService.bookAppointment(request, patientUserId);
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/me")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public List<Appointment> getMyAppointments(HttpServletRequest httpRequest) {
        Long patientUserId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");

        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only patient users can view patient appointments");
        }

        return appointmentService.getAppointmentsByPatient(patientUserId);
    }

    @GetMapping("/{appointmentId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public Appointment getAppointmentById(@PathVariable Long appointmentId) {
        return appointmentService.getAppointmentById(appointmentId);
    }

    @GetMapping("/patient/{patientUserId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByPatient(@PathVariable Long patientUserId) {
        return appointmentService.getAppointmentsByPatient(patientUserId);
    }

    @GetMapping("/provider/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByProvider(@PathVariable Long providerId) {
        return appointmentService.getAppointmentsByProvider(providerId);
    }

    @GetMapping("/status")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<Appointment> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    @GetMapping("/internal/booked-by-date")
    /*
     * This method is used internally by notification service.
     * It returns all active booked appointments for one date
     * so reminder mails can be sent on appointment day.
     */
    public List<Appointment> getBookedAppointmentsForDate(@RequestParam LocalDate date) {
        return appointmentService.getBookedAppointmentsForDate(date);
    }

    @PutMapping("/{appointmentId}/cancel")
    /*
     * This method cancels the existing record or appointment.
     * It is used when the current action should no longer stay active.
     */
    public Appointment cancelAppointment(@PathVariable Long appointmentId, HttpServletRequest httpRequest) {
        Long userId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");
        return appointmentService.cancelAppointment(appointmentId, userId, role);
    }

    @PutMapping("/{appointmentId}/complete")
    /*
     * This method marks the process as completed.
     * It is usually called after the full workflow finishes successfully.
     */
    public Appointment completeAppointment(@PathVariable Long appointmentId) {
        return appointmentService.completeAppointment(appointmentId);
    }

    @PutMapping("/{appointmentId}/internal-release")
    /*
     * This method is used by other services when payment setup fails.
     * It quietly releases the booked slot so the patient is not stuck.
     */
    public Appointment releaseFailedPaymentBooking(@PathVariable Long appointmentId) {
        return appointmentService.releaseFailedPaymentBooking(appointmentId);
    }

    @PutMapping("/{appointmentId}/internal-activate")
    /*
     * This method is used after successful payment verification.
     * It restores the appointment as an active booked slot if it was cancelled during payment retry flow.
     */
    public Appointment activatePaidAppointment(@PathVariable Long appointmentId) {
        return appointmentService.activatePaidAppointment(appointmentId);
    }
}
