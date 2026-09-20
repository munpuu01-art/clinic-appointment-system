package com.clinic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(value = 5, message = "ช่องเวลาต้องอย่างน้อย 5 นาที") int slotMinutes,
        int capacityPerSlot,
        String roomNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo
) { }
