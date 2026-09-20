package com.clinic.domain.billing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.common.BaseEntity;
import com.clinic.domain.person.Patient;
import com.clinic.exception.BusinessRuleException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** ใบแจ้งค่าบริการของการเข้าตรวจ 1 ครั้ง — implements Billable */
@Entity
@Table(name = "invoice")
public class Invoice extends BaseEntity implements Billable {

    @Column(name = "invoice_no", nullable = false, unique = true, length = 20)
    private String invoiceNo;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Payment> payments = new ArrayList<>();

    protected Invoice() { }

    public Invoice(String invoiceNo, Appointment appointment) {
        this.invoiceNo = invoiceNo;
        this.appointment = appointment;
        this.patient = appointment.getPatient();
    }

    public void addItem(InvoiceItem item) {
        requireEditable();
        item.assignTo(this);
        this.items.add(item);
    }

    public void applyDiscount(BigDecimal discount) {
        requireEditable();
        if (discount.signum() < 0) throw new IllegalArgumentException("ส่วนลดต้องไม่ติดลบ");
        this.discount = discount;
    }

    public void issue() {
        if (items.isEmpty()) {
            throw new BusinessRuleException("EMPTY_INVOICE", "ออกใบแจ้งหนี้ไม่ได้ เพราะยังไม่มีรายการค่าบริการ");
        }
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
    }

    public void pay(Payment payment) {
        if (status == InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("INVOICE_NOT_ISSUED", "ต้องออกใบแจ้งหนี้ก่อนรับชำระเงิน");
        }
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.VOID) {
            throw new BusinessRuleException("INVOICE_CLOSED", "ใบแจ้งหนี้นี้ปิดแล้ว");
        }
        if (payment.getAmount().compareTo(outstandingAmount()) > 0) {
            throw new BusinessRuleException("OVERPAY", "ยอดชำระเกินยอดคงค้าง");
        }
        payment.assignTo(this);
        this.payments.add(payment);
        this.status = isSettled() ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
    }

    public void voidInvoice() {
        if (!payments.isEmpty()) {
            throw new BusinessRuleException("VOID_PAID", "ยกเลิกใบแจ้งหนี้ที่มีการชำระแล้วไม่ได้");
        }
        this.status = InvoiceStatus.VOID;
    }

    @Override
    public BigDecimal calculateTotal() {
        BigDecimal sum = items.stream()
                .map(InvoiceItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = sum.subtract(discount == null ? BigDecimal.ZERO : discount);
        return total.signum() < 0 ? BigDecimal.ZERO : total;
    }

    public BigDecimal paidAmount() {
        return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal outstandingAmount() { return calculateTotal().subtract(paidAmount()); }

    @Override
    public boolean isSettled() { return outstandingAmount().signum() <= 0; }

    private void requireEditable() {
        if (status != InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("INVOICE_LOCKED", "แก้ไขได้เฉพาะใบแจ้งหนี้สถานะร่าง");
        }
    }

    public String getInvoiceNo() { return invoiceNo; }
    public Appointment getAppointment() { return appointment; }
    public Patient getPatient() { return patient; }
    public InvoiceStatus getStatus() { return status; }
    public BigDecimal getDiscount() { return discount; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public List<InvoiceItem> getItems() { return items; }
    public List<Payment> getPayments() { return payments; }
}
