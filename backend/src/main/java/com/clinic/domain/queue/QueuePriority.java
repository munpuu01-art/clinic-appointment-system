package com.clinic.domain.queue;

/** ระดับความเร่งด่วนของคิว — ตัวเลขน้อย = ได้ก่อน */
public enum QueuePriority {
    EMERGENCY("ฉุกเฉิน", 0),
    ELDERLY("ผู้สูงอายุ/ตั้งครรภ์/พิการ", 1),
    APPOINTMENT("มีนัดหมาย", 2),
    NORMAL("ทั่วไป (walk-in)", 3);

    private final String label;
    private final int weight;

    QueuePriority(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String getLabel() { return label; }
    public int getWeight() { return weight; }
}
