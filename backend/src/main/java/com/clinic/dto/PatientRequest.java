package com.clinic.dto;

import com.clinic.domain.common.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "กรุณากรอกชื่อ") String firstName,
        @NotBlank(message = "กรุณากรอกนามสกุล") String lastName,
        @NotNull(message = "กรุณาเลือกเพศ") Gender gender,
        @Past(message = "วันเกิดต้องเป็นอดีต") LocalDate birthDate,
        String nationalId,
        String phone,
        String email,
        String lineId,
        String addressLine,
        String district,
        String province,
        String postcode,
        String bloodType,
        String allergies,
        String chronicDisease,
        String emergencyContact
) { }
