package com.clinic.dto;

import java.math.BigDecimal;

public record SpecialtyResponse(
        Long id, String code, String name, int defaultSlotMinutes, BigDecimal baseFee, int doctorCount
) { }
