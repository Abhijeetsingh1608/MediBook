package com.medibook.provider.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medibook.provider.dto.ProviderRequest;
import com.medibook.provider.entity.Provider;
import com.medibook.provider.service.ProviderService;
import java.math.BigDecimal;
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
class ProviderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProviderService providerService;

    @InjectMocks
    private ProviderController providerController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(providerController).build();
    }

    @Test
    void createProvider_success() throws Exception {
        ProviderRequest request = ProviderRequest.builder()
                .fullName("Dr. Smith")
                .specialization("Cardiology")
                .build();
        
        Provider provider = Provider.builder().providerId(1L).fullName("Dr. Smith").build();
        
        when(providerService.createProvider(any(Provider.class))).thenReturn(provider);

        mockMvc.perform(post("/api/v1/providers")
                .header("X-User-Id", "123")
                .header("X-User-Role", "PROVIDER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Smith"));
    }

    @Test
    void getAllProviders_asAdmin_returnsAll() throws Exception {
        Provider p = Provider.builder().providerId(1L).fullName("Dr. A").build();
        when(providerService.getAllProviders()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/providers")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Dr. A"));
    }

    @Test
    void getAllProviders_asUser_returnsVerifiedAndAvailable() throws Exception {
        Provider p = Provider.builder().providerId(1L).fullName("Dr. A").verified(true).available(true).build();
        when(providerService.getProvidersByVerificationStatus(true)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/providers")
                .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Dr. A"));
    }

    @Test
    void getProviderById_success() throws Exception {
        Provider p = Provider.builder().providerId(1L).fullName("Dr. A").verified(true).available(true).build();
        when(providerService.getProviderById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/providers/1")
                .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. A"));
    }

    @Test
    void getProviderByUserId_success() throws Exception {
        Provider p = Provider.builder().providerId(1L).userId(123L).build();
        when(providerService.getProviderByUserId(123L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/providers/user/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123));
    }

    @Test
    void getProvidersBySpecialization_success() throws Exception {
        Provider p = Provider.builder().specialization("Cardio").build();
        when(providerService.getProvidersBySpecialization("Cardio")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/providers/specialization/Cardio"))
                .andExpect(status().isOk());
    }

    @Test
    void searchProviders_success() throws Exception {
        Provider p = Provider.builder().fullName("Smith").build();
        when(providerService.searchProviders("Smith")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/providers/search?keyword=Smith"))
                .andExpect(status().isOk());
    }

    @Test
    void updateVerificationStatus_asAdmin_success() throws Exception {
        Provider p = Provider.builder().providerId(1L).verified(true).build();
        when(providerService.updateVerificationStatus(1L, true)).thenReturn(p);

        mockMvc.perform(put("/api/v1/providers/1/verify?verified=true")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void deleteProvider_success() throws Exception {
        mockMvc.perform(delete("/api/v1/providers/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Provider deleted successfully"));
    }

    @Test
    void updateRating_success() throws Exception {
        Provider p = Provider.builder().providerId(1L).avgRating(BigDecimal.valueOf(4.5)).build();
        when(providerService.updateRating(anyLong(), any(BigDecimal.class))).thenReturn(p);

        mockMvc.perform(put("/api/v1/providers/1/rating?avgRating=4.5"))
                .andExpect(status().isOk());
    }
}
