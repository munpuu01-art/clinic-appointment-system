package com.clinic.dto;

import java.time.LocalDate;

public record MedicalRecordRequest(
        String chiefComplaint,
        String diagnosis,
        String treatment,
        String prescription,
        Double temperatureC,
        Integer systolic,
        Integer diastolic,
        Integer pulse,
        Double weightKg,
        Double heightCm,
        LocalDate followUpDate
) { }
