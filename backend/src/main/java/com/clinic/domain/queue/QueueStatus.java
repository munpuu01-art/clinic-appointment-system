package com.clinic.domain.queue;

public enum QueueStatus {
    WAITING("รอเรียก"),
    CALLED("เรียกแล้ว"),
    SERVING("กำลังรับบริการ"),
    DONE("เสร็จสิ้น"),
    SKIPPED("ข้ามคิว");

    private final String label;
    QueueStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    public boolean isOpen() { return this == WAITING || this == CALLED || this == SERVING; }
}
