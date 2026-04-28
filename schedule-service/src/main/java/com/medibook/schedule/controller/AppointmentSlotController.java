package com.medibook.schedule.controller;

import com.medibook.schedule.dto.ApiMessage;
import com.medibook.schedule.dto.SlotRequest;
import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import com.medibook.schedule.service.AppointmentSlotService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/slots")
@RequiredArgsConstructor
/*
 * This controller handles API requests for AppointmentSlotController.
 * It receives data from frontend, forwards it to service layer,
 * and returns the final response back to the client.
 * Main business logic should not be written here.
 */
public class AppointmentSlotController {

    /*
     * This service dependency is used to reuse business logic from another class.
     */
    private final AppointmentSlotService slotService;

    @PostMapping
    @Operation(summary = "Create appointment slot for a provider")
    /*
     * This method is used to create and save new data.
     * It takes input, prepares the required object,
     * and stores it in database or next layer.
     */
    public AppointmentSlot createSlot(@RequestBody SlotRequest request, HttpServletRequest httpRequest) {
        Long userId = Long.valueOf(httpRequest.getHeader("X-User-Id"));
        String role = httpRequest.getHeader("X-User-Role");

        if (!"PROVIDER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only provider or admin users can create slots");
        }

        return slotService.createSlot(request, userId, role);
    }

    @GetMapping
    /*
     * This method fetches all records for this module.
     * It is mainly used when complete list data is needed on screen.
     */
    public List<AppointmentSlot> getAllSlots() {
        return slotService.getAllSlots();
    }

    @GetMapping("/{slotId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public AppointmentSlot getSlotById(@PathVariable Long slotId) {
        return slotService.getSlotById(slotId);
    }

    @GetMapping("/provider/{providerId}")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<AppointmentSlot> getSlotsByProvider(@PathVariable Long providerId) {
        return slotService.getSlotsByProvider(providerId);
    }

    @GetMapping("/provider/{providerId}/date")
    /*
     * This method fetches data using a specific id or filter value.
     * It is useful when we need one particular record or filtered list.
     */
    public List<AppointmentSlot> getSlotsByProviderAndDate(
            @PathVariable Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return slotService.getSlotsByProviderAndDate(providerId, date);
    }

    @GetMapping("/provider/{providerId}/available")
    /*
     * This method is part of the main flow of this class.
     * It helps complete one specific task of this module.
     */
    public List<AppointmentSlot> getAvailableSlots(
            @PathVariable Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return slotService.getAvailableSlots(providerId, date);
    }

    @PutMapping("/{slotId}")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public AppointmentSlot updateSlot(@PathVariable Long slotId, @RequestBody SlotRequest request) {
        return slotService.updateSlot(slotId, request);
    }

    @PutMapping("/{slotId}/status")
    /*
     * This method updates existing data with new values.
     * It is used when profile, status, or stored details need to change.
     */
    public AppointmentSlot updateSlotStatus(@PathVariable Long slotId, @RequestParam SlotStatus status) {
        return slotService.updateSlotStatus(slotId, status);
    }

    @DeleteMapping("/{slotId}")
    /*
     * This method deletes the selected record from the system.
     * It is usually called when admin or owner removes old data.
     */
    public ApiMessage deleteSlot(@PathVariable Long slotId) {
        slotService.deleteSlot(slotId);
        return ApiMessage.builder()
                .message("Slot deleted successfully")
                .build();
    }
}
