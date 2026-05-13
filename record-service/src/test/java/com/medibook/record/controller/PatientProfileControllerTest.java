package com.medibook.record.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medibook.record.dto.PatientProfileRequest;
import com.medibook.record.entity.PatientProfile;
import com.medibook.record.service.PatientProfileService;
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
class PatientProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PatientProfileService patientProfileService;

    @InjectMocks
    private PatientProfileController patientProfileController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(patientProfileController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/v1/records/patients - success")
    void createProfile_success() throws Exception {
        PatientProfileRequest request = PatientProfileRequest.builder().fullName("John Doe").build();
        PatientProfile response = PatientProfile.builder().patientId(1L).fullName("John Doe").build();

        when(patientProfileService.createProfile(any(), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/records/patients")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/v1/records/patients/me - success")
    void getMyProfile_success() throws Exception {
        PatientProfile response = PatientProfile.builder().patientId(1L).userId(1L).build();
        when(patientProfileService.getProfileByUserId(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/v1/records/patients/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/patients - success")
    void getAllProfiles_success() throws Exception {
        when(patientProfileService.getAllProfiles()).thenReturn(List.of(new PatientProfile()));

        mockMvc.perform(get("/api/v1/records/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/records/patients/{patientUserId} - success")
    void getProfileByUserId_success() throws Exception {
        PatientProfile response = PatientProfile.builder().patientId(1L).userId(2L).build();
        when(patientProfileService.getProfileByUserId(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/records/patients/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    @DisplayName("PUT /api/v1/records/patients/{patientUserId} - success")
    void updateProfile_success() throws Exception {
        PatientProfileRequest request = PatientProfileRequest.builder().fullName("Updated Name").build();
        PatientProfile response = PatientProfile.builder().patientId(1L).fullName("Updated Name").build();

        when(patientProfileService.updateProfile(anyLong(), any(), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(put("/api/v1/records/patients/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }
}
