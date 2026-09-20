package com.clinic.domain.billing;

import com.clinic.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** การชำระเงิน 1 ครั้ง (ใบเสร็จ 1 ใบชำระหลายครั้งได้) */
@Entity
@Table(name = "payment")
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    @Column(name = "reference_no", length = 60)
    private String referenceNo;

    protected Payment() { }

    public Payment(BigDecimal amount, PaymentMethod method, String referenceNo) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("ยอดชำระต้องมากกว่า 0");
        }
        this.amount = amount;
        this.method = method;
        this.referenceNo = referenceNo;
    }

    void assignTo(Invoice invoice) { this.invoice = invoice; }

    public Invoice getInvoice() { return invoice; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getReferenceNo() { return referenceNo; }
}
