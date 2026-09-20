package com.clinic.repository;

import com.clinic.domain.billing.Invoice;
import com.clinic.domain.billing.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByAppointmentId(Long appointmentId);
    Optional<Invoice> findByInvoiceNo(String invoiceNo);
    List<Invoice> findByPatientId(Long patientId);
    List<Invoice> findByStatus(InvoiceStatus status);
}
