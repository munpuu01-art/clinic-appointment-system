package com.clinic.dto;

import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.domain.appointment.AppointmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AppointmentResponse(
        Long id,
        String appointmentNo,
        Long patientId,
        String patientName,
        String hn,
        Long doctorId,
        String doctorName,
        String specialtyName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentType type,
        String typeLabel,
        AppointmentStatus status,
        String statusLabel,
        String symptomNote,
        String cancelReason,
        BigDecimal fee,
        String ticketNo,
        List<AppointmentStatus> allowedTransitions
) { }
