package com.medibook.schedule.service;

import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import java.time.LocalDate;
import java.util.List;

/*
 * This is the service interface for AppointmentSlotService.
 * It tells what operations are available in this module.
 * Actual business logic will be written in the implementation class.
 * This helps keep the contract clear between controller and service layer.
 */
public interface AppointmentSlotService {

    AppointmentSlot createSlot(SlotRequest request, Long createdByUserId, String role);

    List<AppointmentSlot> getAllSlots();

    AppointmentSlot getSlotById(Long slotId);

    List<AppointmentSlot> getSlotsByProvider(Long providerId);

    List<AppointmentSlot> getSlotsByProviderAndDate(Long providerId, LocalDate slotDate);

    List<AppointmentSlot> getAvailableSlots(Long providerId, LocalDate slotDate);

    AppointmentSlot updateSlot(Long slotId, SlotRequest request);

    AppointmentSlot updateSlotStatus(Long slotId, SlotStatus status);

    void deleteSlot(Long slotId);
}
