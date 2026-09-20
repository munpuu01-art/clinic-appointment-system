package com.clinic.controller;

import com.clinic.dto.InvoiceItemRequest;
import com.clinic.dto.InvoiceResponse;
import com.clinic.dto.PaymentRequest;
import com.clinic.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) { this.billingService = billingService; }

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@RequestParam Long appointmentId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingService.createForAppointment(appointmentId));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public InvoiceResponse byAppointment(@PathVariable Long appointmentId) {
        return billingService.findByAppointment(appointmentId);
    }

    @PostMapping("/{id}/items")
    public InvoiceResponse addItem(@PathVariable Long id, @Valid @RequestBody InvoiceItemRequest request) {
        return billingService.addItem(id, request);
    }

    @PatchMapping("/{id}/issue")
    public InvoiceResponse issue(@PathVariable Long id) { return billingService.issue(id); }

    @PostMapping("/{id}/payments")
    public InvoiceResponse pay(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        return billingService.pay(id, request);
    }
}
