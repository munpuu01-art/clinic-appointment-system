package com.clinic.dto;

import com.clinic.domain.common.Gender;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String hn,
        String fullName,
        Gender gender,
        LocalDate birthDate,
        Integer age,
        boolean elderly,
        String phone,
        String email,
        String address,
        String bloodType,
        String allergies,
        String chronicDisease,
        long visitCount
) { }
