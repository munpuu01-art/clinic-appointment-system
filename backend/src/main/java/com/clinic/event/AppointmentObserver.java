package com.clinic.event;

/**
 * Observer Pattern: ผู้สนใจเหตุการณ์ของนัดหมาย
 * เพิ่มช่องทางแจ้งเตือนใหม่ได้โดยไม่แตะ AppointmentService
 */
public interface AppointmentObserver {
    String observerName();
    boolean supports(AppointmentEventType type);
    void onEvent(AppointmentEvent event);
}
