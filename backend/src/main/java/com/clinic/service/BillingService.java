package com.clinic.service;

import com.clinic.dto.InvoiceItemRequest;
import com.clinic.dto.InvoiceResponse;
import com.clinic.dto.PaymentRequest;

public interface BillingService {
    InvoiceResponse createForAppointment(Long appointmentId);
    InvoiceResponse addItem(Long invoiceId, InvoiceItemRequest request);
    InvoiceResponse issue(Long invoiceId);
    InvoiceResponse pay(Long invoiceId, PaymentRequest request);
    InvoiceResponse findByAppointment(Long appointmentId);
}
