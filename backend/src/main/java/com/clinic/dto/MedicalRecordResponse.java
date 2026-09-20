package com.clinic.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicalRecordResponse(
        Long id,
        Long appointmentId,
        String appointmentNo,
        String patientName,
        String doctorName,
        String chiefComplaint,
        String diagnosis,
        String treatment,
        String prescription,
        Double temperatureC,
        Integer systolic,
        Integer diastolic,
        Double bmi,
        LocalDate followUpDate,
        LocalDateTime createdAt
) { }
