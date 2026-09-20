package com.clinic.dto;

import com.clinic.domain.common.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DoctorRequest(
        @NotBlank(message = "กรุณากรอกเลขใบประกอบวิชาชีพ") String licenseNo,
        @NotNull(message = "กรุณาเลือกแผนก") Long specialtyId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        Gender gender,
        LocalDate birthDate,
        String nationalId,
        String phone,
        String email,
        BigDecimal consultationFee,
        String roomNo,
        String biography
) { }
