package com.clinic.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        Long doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotMinutes,
        int capacityPerSlot,
        String roomNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        boolean active,
        int totalCapacity
) { }
