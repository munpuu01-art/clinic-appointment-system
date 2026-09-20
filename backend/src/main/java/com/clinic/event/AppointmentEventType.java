package com.clinic.event;

public enum AppointmentEventType {
    BOOKED("จองนัดหมายสำเร็จ"),
    CONFIRMED("ยืนยันนัดหมาย"),
    RESCHEDULED("เลื่อนนัดหมาย"),
    CANCELLED("ยกเลิกนัดหมาย"),
    CHECKED_IN("เช็คอินและรับบัตรคิว"),
    QUEUE_CALLED("เรียกคิว"),
    COMPLETED("ตรวจเสร็จสิ้น"),
    NO_SHOW("ไม่มาตามนัด");

    private final String label;
    AppointmentEventType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
