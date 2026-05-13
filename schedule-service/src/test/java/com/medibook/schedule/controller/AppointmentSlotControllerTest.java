package com.medibook.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import com.medibook.schedule.service.AppointmentSlotService;
import java.time.LocalDate;
import java.time.LocalTime;
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
class AppointmentSlotControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AppointmentSlotService slotService;

    @InjectMocks
    private AppointmentSlotController slotController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(slotController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createSlot_success() throws Exception {
        SlotRequest request = SlotRequest.builder()
                .providerId(1L)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .build();
        
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).build();
        
        when(slotService.createSlot(any(SlotRequest.class), anyLong(), anyString())).thenReturn(slot);

        mockMvc.perform(post("/api/v1/slots")
                .header("X-User-Id", "123")
                .header("X-User-Role", "PROVIDER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(1));
    }

    @Test
    void getAllSlots_success() throws Exception {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).build();
        when(slotService.getAllSlots()).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/v1/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slotId").value(1));
    }

    @Test
    void getSlotById_success() throws Exception {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).build();
        when(slotService.getSlotById(1L)).thenReturn(slot);

        mockMvc.perform(get("/api/v1/slots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(1));
    }

    @Test
    void getSlotsByProvider_success() throws Exception {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).providerId(1L).build();
        when(slotService.getSlotsByProvider(1L)).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/v1/slots/provider/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateSlotStatus_success() throws Exception {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).status(SlotStatus.BOOKED).build();
        when(slotService.updateSlotStatus(1L, SlotStatus.BOOKED)).thenReturn(slot);

        mockMvc.perform(put("/api/v1/slots/1/status?status=BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    void deleteSlot_success() throws Exception {
        mockMvc.perform(delete("/api/v1/slots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Slot deleted successfully"));
    }
}
