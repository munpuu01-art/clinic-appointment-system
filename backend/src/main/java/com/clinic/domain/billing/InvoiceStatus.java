package com.clinic.domain.billing;

public enum InvoiceStatus {
    DRAFT("ร่าง"), ISSUED("รอชำระ"), PARTIALLY_PAID("ชำระบางส่วน"), PAID("ชำระแล้ว"), VOID("ยกเลิก");

    private final String label;
    InvoiceStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
