package com.medibook.schedule.repository;

import com.medibook.schedule.entity.AppointmentSlot;
import com.medibook.schedule.entity.SlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * This is the service interface for AppointmentSlotRepository.
 * It only tells what operations are available in this module.
 * Real business logic will be written in the implementation class.
 */
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByProviderId(Long providerId);

    List<AppointmentSlot> findByProviderIdAndSlotDate(Long providerId, LocalDate slotDate);

    List<AppointmentSlot> findByProviderIdAndSlotDateAndStatus(
            Long providerId, LocalDate slotDate, SlotStatus status);

    boolean existsByProviderIdAndSlotDateAndStartTimeAndEndTime(
            Long providerId, LocalDate slotDate, LocalTime startTime, LocalTime endTime);
}
