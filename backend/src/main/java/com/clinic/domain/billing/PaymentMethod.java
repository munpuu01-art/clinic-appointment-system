package com.clinic.domain.billing;

public enum PaymentMethod {
    CASH("เงินสด"), CREDIT_CARD("บัตรเครดิต"), PROMPTPAY("พร้อมเพย์"), INSURANCE("ประกัน/สิทธิ์");

    private final String label;
    PaymentMethod(String label) { this.label = label; }
    public String getLabel() { return label; }
}
