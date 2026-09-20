package com.clinic.dto;

import com.clinic.domain.billing.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull BigDecimal amount,
        @NotNull PaymentMethod method,
        String referenceNo
) { }
