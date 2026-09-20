package com.clinic.dto;

import com.clinic.domain.appointment.AppointmentType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** ผู้ป่วยจองนัดเอง — ไม่ต้องส่ง patientId เพราะระบบอ่านจาก token */
public record SelfBookingRequest(
        @NotNull(message = "กรุณาเลือกแพทย์") Long doctorId,
        @NotNull(message = "กรุณาเลือกวันที่") LocalDate date,
        @NotNull(message = "กรุณาเลือกเวลา") LocalTime startTime,
        AppointmentType type,
        String symptomNote
) { }
