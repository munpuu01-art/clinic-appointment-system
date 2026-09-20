package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;

/** รอยืนยัน: ยืนยันหรือยกเลิกได้ */
public class RequestedState extends AbstractAppointmentState {

    @Override public AppointmentStatus status() { return AppointmentStatus.REQUESTED; }

    @Override
    public void confirm(Appointment a) { a.applyStatus(AppointmentStatus.CONFIRMED); }

    @Override
    public void cancel(Appointment a, String reason) {
        a.applyCancelReason(reason);
        a.applyStatus(AppointmentStatus.CANCELLED);
    }

    @Override
    public boolean allows(AppointmentStatus target) {
        return target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED;
    }
}
