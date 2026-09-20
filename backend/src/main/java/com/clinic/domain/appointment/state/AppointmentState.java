package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;

/**
 * State Pattern — สัญญาของทุกสถานะนัดหมาย
 * ทำให้ workflow ไม่กลายเป็น if-else ยักษ์ใน Service
 */
public interface AppointmentState {

    AppointmentStatus status();

    void confirm(Appointment appointment);
    void checkIn(Appointment appointment);
    void start(Appointment appointment);
    void complete(Appointment appointment);
    void cancel(Appointment appointment, String reason);
    void noShow(Appointment appointment);

    default boolean allows(AppointmentStatus target) { return false; }
}
