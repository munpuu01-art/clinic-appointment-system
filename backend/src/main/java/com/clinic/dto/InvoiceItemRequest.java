package com.clinic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record InvoiceItemRequest(
        @NotBlank String description,
        @Min(1) int quantity,
        BigDecimal unitPrice
) { }
