package com.clinic.dto;

import java.math.BigDecimal;
import java.util.List;

public record DoctorResponse(
        Long id,
        String licenseNo,
        String fullName,
        String displayName,
        String specialtyName,
        Long specialtyId,
        BigDecimal consultationFee,
        String roomNo,
        boolean active,
        List<ScheduleResponse> schedules
) { }
