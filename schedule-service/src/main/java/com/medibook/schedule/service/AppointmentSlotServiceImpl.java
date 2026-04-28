package com.medibook.schedule.service;

import com.medibook.schedule.client.ProviderClient;
import com.medibook.schedule.dto.ProviderSummary;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import com.medibook.schedule.repository.AppointmentSlotRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/*
 * This is the actual implementation class for AppointmentSlotServiceImpl.
 * All the real business logic is written here.
 * Controller calls this class whenever some processing, validation,
 * database save, or microservice call is needed.
 */
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    /*
     * This repository object is used to interact with database.
     * It gives us save, update, delete, and fetch methods for this module.
     */
    private final AppointmentSlotRepository slotRepository;
    /*
     * This client is used to call another microservice from this class.
     * It helps connect modules without putting remote call logic everywhere.
     */
    private final ProviderClient providerClient;

    @Override
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public AppointmentSlot createSlot(SlotRequest request, Long createdByUserId, String role) {
        validateSlotRequest(request);
        validateProviderCanCreateSlot(request.getProviderId(), createdByUserId, role);

        boolean exists = slotRepository.existsByProviderIdAndSlotDateAndStartTimeAndEndTime(
                request.getProviderId(),
                request.getSlotDate(),
                request.getStartTime(),
                request.getEndTime());

        if (exists) {
            throw new RuntimeException("Slot already exists for this provider and time");
        }

        AppointmentSlot slot = AppointmentSlot.builder()
                .providerId(request.getProviderId())
                .createdByUserId(createdByUserId)
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(SlotStatus.AVAILABLE)
                .build();

        return slotRepository.save(slot);
    }

    @Override
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<AppointmentSlot> getAllSlots() {
        return slotRepository.findAll();
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public AppointmentSlot getSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<AppointmentSlot> getSlotsByProvider(Long providerId) {
        return slotRepository.findByProviderId(providerId);
    }

    @Override
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<AppointmentSlot> getSlotsByProviderAndDate(Long providerId, LocalDate slotDate) {
        return slotRepository.findByProviderIdAndSlotDate(providerId, slotDate);
    }

    @Override
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public List<AppointmentSlot> getAvailableSlots(Long providerId, LocalDate slotDate) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return slotRepository.findByProviderIdAndSlotDateAndStatus(
                        providerId, slotDate, SlotStatus.AVAILABLE)
                .stream()
                .filter(slot -> !slotDate.isBefore(today))
                .filter(slot -> !slotDate.equals(today) || slot.getStartTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public AppointmentSlot updateSlot(Long slotId, SlotRequest request) {
        validateSlotRequest(request);

        AppointmentSlot slot = getSlotById(slotId);
        slot.setProviderId(request.getProviderId());
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        return slotRepository.save(slot);
    }

    @Override
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public AppointmentSlot updateSlotStatus(Long slotId, SlotStatus status) {
        AppointmentSlot slot = getSlotById(slotId);
        slot.setStatus(status);
        return slotRepository.save(slot);
    }

    @Override
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public void deleteSlot(Long slotId) {
        AppointmentSlot slot = getSlotById(slotId);
        slotRepository.delete(slot);
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateSlotRequest(SlotRequest request) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getProviderId() == null) {
            throw new RuntimeException("Provider id is required");
        }
        if (request.getSlotDate() == null) {
            throw new RuntimeException("Slot date is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }
        if (request.getSlotDate().isBefore(today)) {
            throw new RuntimeException("Past date slots cannot be created");
        }
        if (request.getSlotDate().equals(today) && !request.getStartTime().isAfter(now)) {
            throw new RuntimeException("Past time slots cannot be created for today");
        }
    }

    /*
     * This helper method checks the rules before main logic continues.
     * It helps stop invalid data or unauthorized access early.
     */
    private void validateProviderCanCreateSlot(Long providerId, Long createdByUserId, String role) {
        ProviderSummary provider = providerClient.getProviderById(providerId);
        if (provider == null) {
            throw new RuntimeException("Provider not found");
        }

        if (!provider.isVerified()) {
            throw new RuntimeException("Provider must be approved by admin before creating slots");
        }

        if (!provider.isAvailable()) {
            throw new RuntimeException("Provider must be available before creating slots");
        }

        if ("PROVIDER".equalsIgnoreCase(role) && !provider.getUserId().equals(createdByUserId)) {
            throw new RuntimeException("You can create slots only for your own provider profile");
        }
    }
}
