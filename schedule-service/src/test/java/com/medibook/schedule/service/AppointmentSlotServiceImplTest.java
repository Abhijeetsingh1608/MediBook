package com.medibook.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medibook.schedule.client.ProviderClient;
import com.medibook.schedule.dto.ProviderSummary;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import com.medibook.schedule.repository.AppointmentSlotRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentSlotServiceImplTest {

    @Mock
    private AppointmentSlotRepository slotRepository;

    @Mock
    private ProviderClient providerClient;

    @InjectMocks
    private AppointmentSlotServiceImpl slotService;

    private SlotRequest sampleRequest;
    private ProviderSummary providerSummary;

    @BeforeEach
    void setUp() {
        sampleRequest = SlotRequest.builder()
                .providerId(1L)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .build();

        providerSummary = ProviderSummary.builder()
                .providerId(1L)
                .userId(10L)
                .fullName("Dr. Sample")
                .verified(true)
                .available(true)
                .build();
    }

    @Test
    @DisplayName("createSlot: success - saves available slot for verified provider")
    void createSlot_success() {
        when(providerClient.getProviderById(1L)).thenReturn(providerSummary);
        when(slotRepository.existsByProviderIdAndSlotDateAndStartTimeAndEndTime(
                1L, sampleRequest.getSlotDate(), sampleRequest.getStartTime(), sampleRequest.getEndTime()))
                .thenReturn(false);
        when(slotRepository.save(any(AppointmentSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentSlot result = slotService.createSlot(sampleRequest, 10L, "PROVIDER");

        assertThat(result.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(result.getProviderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("createSlot: throws when slot already exists")
    void createSlot_duplicate_throwsException() {
        when(providerClient.getProviderById(1L)).thenReturn(providerSummary);
        when(slotRepository.existsByProviderIdAndSlotDateAndStartTimeAndEndTime(
                1L, sampleRequest.getSlotDate(), sampleRequest.getStartTime(), sampleRequest.getEndTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> slotService.createSlot(sampleRequest, 10L, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot already exists");

        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSlot: throws when provider is not verified")
    void createSlot_unverifiedProvider_throwsException() {
        providerSummary.setVerified(false);
        when(providerClient.getProviderById(1L)).thenReturn(providerSummary);

        assertThatThrownBy(() -> slotService.createSlot(sampleRequest, 10L, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("approved by admin");
    }

    @Test
    @DisplayName("getAvailableSlots: filters out past slots for today")
    void getAvailableSlots_filtersPastTodaySlots() {
        LocalDate today = LocalDate.now();
        AppointmentSlot pastSlot = AppointmentSlot.builder()
                .slotId(1L).providerId(1L).slotDate(today)
                .startTime(LocalTime.now().minusMinutes(30))
                .endTime(LocalTime.now().minusMinutes(10))
                .status(SlotStatus.AVAILABLE).build();
        AppointmentSlot futureSlot = AppointmentSlot.builder()
                .slotId(2L).providerId(1L).slotDate(today)
                .startTime(LocalTime.now().plusMinutes(30))
                .endTime(LocalTime.now().plusMinutes(60))
                .status(SlotStatus.AVAILABLE).build();

        when(slotRepository.findByProviderIdAndSlotDateAndStatus(1L, today, SlotStatus.AVAILABLE))
                .thenReturn(List.of(pastSlot, futureSlot));

        List<AppointmentSlot> result = slotService.getAvailableSlots(1L, today);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlotId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateSlotStatus: updates status and saves")
    void updateSlotStatus_success() {
        AppointmentSlot slot = AppointmentSlot.builder()
                .slotId(1L).providerId(1L).status(SlotStatus.AVAILABLE).build();
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(AppointmentSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentSlot result = slotService.updateSlotStatus(1L, SlotStatus.BOOKED);

        assertThat(result.getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void getAllSlots_success() {
        when(slotRepository.findAll()).thenReturn(List.of(AppointmentSlot.builder().slotId(1L).build()));
        List<AppointmentSlot> result = slotService.getAllSlots();
        assertThat(result).hasSize(1);
    }

    @Test
    void getSlotById_success() {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).build();
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        AppointmentSlot result = slotService.getSlotById(1L);
        assertThat(result.getSlotId()).isEqualTo(1L);
    }

    @Test
    void getSlotById_notFound_throwsException() {
        when(slotRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> slotService.getSlotById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getSlotsByProvider_success() {
        when(slotRepository.findByProviderId(1L)).thenReturn(List.of(AppointmentSlot.builder().slotId(1L).build()));
        List<AppointmentSlot> result = slotService.getSlotsByProvider(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void getSlotsByProviderAndDate_success() {
        LocalDate date = LocalDate.now();
        when(slotRepository.findByProviderIdAndSlotDate(1L, date)).thenReturn(List.of(AppointmentSlot.builder().slotId(1L).build()));
        List<AppointmentSlot> result = slotService.getSlotsByProviderAndDate(1L, date);
        assertThat(result).hasSize(1);
    }

    @Test
    void updateSlot_success() {
        AppointmentSlot existing = AppointmentSlot.builder().slotId(1L).build();
        when(slotRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(slotRepository.save(any(AppointmentSlot.class))).thenAnswer(i -> i.getArgument(0));

        SlotRequest request = SlotRequest.builder()
                .providerId(1L)
                .slotDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .build();

        AppointmentSlot result = slotService.updateSlot(1L, request);
        assertThat(result.getSlotDate()).isEqualTo(request.getSlotDate());
    }

    @Test
    void deleteSlot_success() {
        AppointmentSlot slot = AppointmentSlot.builder().slotId(1L).build();
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        slotService.deleteSlot(1L);
        verify(slotRepository).delete(slot);
    }

    @Test
    void createSlot_unauthorizedProvider_throwsException() {
        when(providerClient.getProviderById(1L)).thenReturn(providerSummary);
        assertThatThrownBy(() -> slotService.createSlot(sampleRequest, 99L, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("your own provider profile");
    }

    @Test
    void createSlot_invalidTimeRange_throwsException() {
        sampleRequest.setEndTime(sampleRequest.getStartTime().minusMinutes(1));
        assertThatThrownBy(() -> slotService.createSlot(sampleRequest, 10L, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("after start time");
    }

    @Test
    void createSlot_pastDate_throwsException() {
        sampleRequest.setSlotDate(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> slotService.createSlot(sampleRequest, 10L, "PROVIDER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Past date slots");
    }
}
