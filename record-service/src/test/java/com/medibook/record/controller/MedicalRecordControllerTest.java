package com.medibook.record.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medibook.record.dto.MedicalRecordRequest;
import com.medibook.record.entity.MedicalRecord;
import com.medibook.record.service.MedicalRecordService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicalRecordService medicalRecordService;

    @InjectMocks
    private MedicalRecordController medicalRecordController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(medicalRecordController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/records/medical - success")
    void createMedicalRecord_success() throws Exception {
        MedicalRecordRequest request = MedicalRecordRequest.builder()
                .appointmentId(1L)
                .symptoms("Cough")
                .build();
        MedicalRecord response = MedicalRecord.builder().recordId(1L).build();

        when(medicalRecordService.createMedicalRecord(any(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/records/medical")
                        .header("X-User-Role", "PROVIDER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/medical - success")
    void getAllMedicalRecords_success() throws Exception {
        when(medicalRecordService.getAllMedicalRecords()).thenReturn(List.of(new MedicalRecord()));

        mockMvc.perform(get("/api/v1/records/medical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/medical/{recordId} - success")
    void getMedicalRecordById_success() throws Exception {
        MedicalRecord record = MedicalRecord.builder().recordId(1L).build();
        when(medicalRecordService.getMedicalRecordById(anyLong(), anyLong(), anyString())).thenReturn(record);

        mockMvc.perform(get("/api/v1/records/medical/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/medical/patient/{patientUserId} - success")
    void getRecordsByPatient_success() throws Exception {
        when(medicalRecordService.getRecordsByPatient(anyLong(), anyLong(), anyString())).thenReturn(List.of(new MedicalRecord()));

        mockMvc.perform(get("/api/v1/records/medical/patient/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/medical/provider/{providerId} - success")
    void getRecordsByProvider_success() throws Exception {
        when(medicalRecordService.getRecordsByProvider(anyLong())).thenReturn(List.of(new MedicalRecord()));

        mockMvc.perform(get("/api/v1/records/medical/provider/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/records/medical/{recordId} - success")
    void updateMedicalRecord_success() throws Exception {
        MedicalRecordRequest request = MedicalRecordRequest.builder()
                .appointmentId(1L)
                .symptoms("Updated Cough")
                .build();
        MedicalRecord response = MedicalRecord.builder().recordId(1L).symptoms("Updated Cough").build();

        when(medicalRecordService.updateMedicalRecord(anyLong(), any(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/v1/records/medical/1")
                        .header("X-User-Role", "PROVIDER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms").value("Updated Cough"));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/medical/{recordId} - success")
    void deleteMedicalRecord_success() throws Exception {
        mockMvc.perform(delete("/api/v1/records/medical/1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Medical record deleted successfully"));
    }
}
