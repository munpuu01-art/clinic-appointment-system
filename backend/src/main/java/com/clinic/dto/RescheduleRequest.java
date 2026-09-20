package com.clinic.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        String reason
) { }
