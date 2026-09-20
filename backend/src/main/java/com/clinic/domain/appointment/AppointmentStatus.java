package com.clinic.domain.appointment;

/** สถานะนัดหมายตาม workflow ของคลินิก */
public enum AppointmentStatus {
    REQUESTED("รอยืนยัน"),
    CONFIRMED("ยืนยันแล้ว"),
    CHECKED_IN("เช็คอินแล้ว (รอคิว)"),
    IN_PROGRESS("กำลังตรวจ"),
    COMPLETED("ตรวจเสร็จ"),
    CANCELLED("ยกเลิก"),
    NO_SHOW("ไม่มาตามนัด");

    private final String label;
    AppointmentStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    public boolean isFinal() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
    }
    public boolean isActive() { return !isFinal(); }
}
