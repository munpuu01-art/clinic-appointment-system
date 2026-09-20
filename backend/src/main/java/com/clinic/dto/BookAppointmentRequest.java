package com.clinic.dto;

import com.clinic.domain.appointment.AppointmentType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookAppointmentRequest(
        @NotNull(message = "กรุณาเลือกผู้ป่วย") Long patientId,
        @NotNull(message = "กรุณาเลือกแพทย์") Long doctorId,
        @NotNull(message = "กรุณาเลือกวันที่") LocalDate date,
        @NotNull(message = "กรุณาเลือกเวลา") LocalTime startTime,
        @NotNull(message = "กรุณาเลือกประเภทการเข้ารับบริการ") AppointmentType type,
        String symptomNote,
        String createdBy
) { }
