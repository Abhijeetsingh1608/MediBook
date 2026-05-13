package com.medibook.appointment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.appointment.dto.AppointmentRequest;
import com.medibook.appointment.entity.Appointment;
import com.medibook.appointment.entity.AppointmentStatus;
import com.medibook.appointment.service.AppointmentService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setControllerAdvice(new com.medibook.appointment.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void bookAppointment_success() throws Exception {
        AppointmentRequest request = new AppointmentRequest(1L, 1L, "Checkup");
        Appointment appointment = Appointment.builder().appointmentId(1L).build();

        when(appointmentService.bookAppointment(any(), eq(10L))).thenReturn(appointment);

        mockMvc.perform(post("/api/v1/appointments")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1));
    }

    @Test
    void getAllAppointments_success() throws Exception {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(Appointment.builder().appointmentId(1L).build()));

        mockMvc.perform(get("/api/v1/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(1));
    }

    @Test
    void getMyAppointments_success() throws Exception {
        when(appointmentService.getAppointmentsByPatient(10L)).thenReturn(List.of(Appointment.builder().appointmentId(1L).build()));

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].appointmentId").value(1));
    }

    @Test
    void getAppointmentById_success() throws Exception {
        when(appointmentService.getAppointmentById(1L)).thenReturn(Appointment.builder().appointmentId(1L).build());

        mockMvc.perform(get("/api/v1/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1));
    }

    @Test
    void getAppointmentsByStatus_success() throws Exception {
        when(appointmentService.getAppointmentsByStatus(AppointmentStatus.BOOKED))
                .thenReturn(List.of(Appointment.builder().appointmentId(1L).status(AppointmentStatus.BOOKED).build()));

        mockMvc.perform(get("/api/v1/appointments/status?status=BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("BOOKED"));
    }

    @Test
    void cancelAppointment_success() throws Exception {
        when(appointmentService.cancelAppointment(eq(1L), eq(10L), eq("PATIENT")))
                .thenReturn(Appointment.builder().appointmentId(1L).status(AppointmentStatus.CANCELLED).build());

        mockMvc.perform(put("/api/v1/appointments/1/cancel")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void bookAppointment_notPatient_throwsException() throws Exception {
        AppointmentRequest request = new AppointmentRequest(1L, 1L, "Checkup");

        mockMvc.perform(post("/api/v1/appointments")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PROVIDER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyAppointments_notPatient_throwsException() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/me")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "PROVIDER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeAppointment_success() throws Exception {
        when(appointmentService.completeAppointment(1L))
                .thenReturn(Appointment.builder().appointmentId(1L).status(AppointmentStatus.COMPLETED).build());

        mockMvc.perform(put("/api/v1/appointments/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
