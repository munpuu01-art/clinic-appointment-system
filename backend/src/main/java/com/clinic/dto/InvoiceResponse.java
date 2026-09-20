package com.clinic.dto;

import com.clinic.domain.billing.InvoiceStatus;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNo,
        Long appointmentId,
        String patientName,
        InvoiceStatus status,
        String statusLabel,
        List<Line> items,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal paid,
        BigDecimal outstanding
) {
    public record Line(String description, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) { }
}
