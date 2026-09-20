package com.clinic.event;

import com.clinic.domain.appointment.Appointment;

import java.time.LocalDateTime;

/** เหตุการณ์ที่เกิดกับนัดหมาย 1 ครั้ง */
public record AppointmentEvent(
        AppointmentEventType type,
        Appointment appointment,
        String detail,
        LocalDateTime occurredAt
) {
    public static AppointmentEvent of(AppointmentEventType type, Appointment appointment) {
        return new AppointmentEvent(type, appointment, null, LocalDateTime.now());
    }

    public static AppointmentEvent of(AppointmentEventType type, Appointment appointment, String detail) {
        return new AppointmentEvent(type, appointment, detail, LocalDateTime.now());
    }
}
