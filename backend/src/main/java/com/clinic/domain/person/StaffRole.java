package com.clinic.domain.person;

public enum StaffRole {
    RECEPTIONIST("เจ้าหน้าที่เวชระเบียน"),
    NURSE("พยาบาล"),
    CASHIER("การเงิน"),
    ADMIN("ผู้ดูแลระบบ");

    private final String label;
    StaffRole(String label) { this.label = label; }
    public String getLabel() { return label; }
}
