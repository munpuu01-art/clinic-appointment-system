package com.clinic.domain.billing;

import com.clinic.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

/** รายการในใบเสร็จ เช่น ค่าตรวจ ค่ายา ค่าหัตถการ */
@Entity
@Table(name = "invoice_item")
public class InvoiceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    protected InvoiceItem() { }

    public InvoiceItem(String description, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("จำนวนต้องมากกว่า 0");
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void assignTo(Invoice invoice) { this.invoice = invoice; }

    public BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }

    public Invoice getInvoice() { return invoice; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
