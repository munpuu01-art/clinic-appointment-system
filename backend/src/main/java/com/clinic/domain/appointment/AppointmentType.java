package com.clinic.domain.appointment;

/** ประเภทการเข้ารับบริการ — มีผลต่อการคิดค่าบริการและลำดับคิว */
public enum AppointmentType {
    NEW_CASE("ผู้ป่วยใหม่", 1.0),
    FOLLOW_UP("ติดตามอาการ", 0.5),
    WALK_IN("Walk-in", 1.0),
    URGENT("เร่งด่วน", 1.3),
    TELEMEDICINE("ปรึกษาออนไลน์", 0.8);

    private final String label;
    private final double feeFactor;

    AppointmentType(String label, double feeFactor) {
        this.label = label;
        this.feeFactor = feeFactor;
    }

    public String getLabel() { return label; }
    public double getFeeFactor() { return feeFactor; }
    public boolean requiresSchedule() { return this != WALK_IN; }
}
