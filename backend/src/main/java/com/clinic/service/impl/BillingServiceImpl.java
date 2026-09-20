package com.clinic.service.impl;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.billing.Invoice;
import com.clinic.domain.billing.InvoiceItem;
import com.clinic.domain.billing.Payment;
import com.clinic.dto.InvoiceItemRequest;
import com.clinic.dto.InvoiceResponse;
import com.clinic.dto.PaymentRequest;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.factory.DocumentNumberGenerator;
import com.clinic.mapper.DomainMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.InvoiceRepository;
import com.clinic.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final DocumentNumberGenerator numberGenerator;
    private final DomainMapper mapper;

    public BillingServiceImpl(InvoiceRepository invoiceRepository,
                              AppointmentRepository appointmentRepository,
                              DocumentNumberGenerator numberGenerator,
                              DomainMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.appointmentRepository = appointmentRepository;
        this.numberGenerator = numberGenerator;
        this.mapper = mapper;
    }

    /** เปิดใบแจ้งค่าบริการ พร้อมใส่ค่าตรวจแพทย์เป็นรายการแรกให้อัตโนมัติ */
    @Override
    public InvoiceResponse createForAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("นัดหมาย", appointmentId));

        Invoice invoice = invoiceRepository.findByAppointmentId(appointmentId)
                .orElseGet(() -> {
                    Invoice inv = new Invoice(
                            numberGenerator.nextInvoiceNo(appointment.getAppointmentDate()), appointment);
                    BigDecimal fee = appointment.getFee() != null
                            ? appointment.getFee() : appointment.getDoctor().effectiveFee();
                    inv.addItem(new InvoiceItem("ค่าตรวจ " + appointment.getDoctor().getSpecialty().getName(), 1, fee));
                    return inv;
                });

        return mapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse addItem(Long invoiceId, InvoiceItemRequest r) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.addItem(new InvoiceItem(r.description(), r.quantity(), r.unitPrice()));
        return mapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse issue(Long invoiceId) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.issue();
        return mapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceResponse pay(Long invoiceId, PaymentRequest r) {
        Invoice invoice = getInvoice(invoiceId);
        invoice.pay(new Payment(r.amount(), r.method(), r.referenceNo()));
        return mapper.toDto(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse findByAppointment(Long appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId).map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("ใบแจ้งค่าบริการของนัดหมาย", appointmentId));
    }

    private Invoice getInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ใบแจ้งค่าบริการ", id));
    }
}
