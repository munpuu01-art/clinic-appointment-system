package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;

/** เช็คอินแล้ว (อยู่ในคิว): เรียกเข้าตรวจ หรือยกเลิกโดยเจ้าหน้าที่ */
public class CheckedInState extends AbstractAppointmentState {

    @Override public AppointmentStatus status() { return AppointmentStatus.CHECKED_IN; }

    @Override
    public void start(Appointment a) { a.applyStatus(AppointmentStatus.IN_PROGRESS); }

    @Override
    public void cancel(Appointment a, String reason) {
        a.applyCancelReason(reason);
        a.applyStatus(AppointmentStatus.CANCELLED);
    }

    @Override
    public void noShow(Appointment a) { a.applyStatus(AppointmentStatus.NO_SHOW); }

    @Override
    public boolean allows(AppointmentStatus target) {
        return target == AppointmentStatus.IN_PROGRESS
                || target == AppointmentStatus.CANCELLED
                || target == AppointmentStatus.NO_SHOW;
    }
}
