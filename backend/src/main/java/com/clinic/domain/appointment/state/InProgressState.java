package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;

/** กำลังตรวจ: ปิดการตรวจได้อย่างเดียว */
public class InProgressState extends AbstractAppointmentState {

    @Override public AppointmentStatus status() { return AppointmentStatus.IN_PROGRESS; }

    @Override
    public void complete(Appointment a) { a.applyStatus(AppointmentStatus.COMPLETED); }

    @Override
    public boolean allows(AppointmentStatus target) { return target == AppointmentStatus.COMPLETED; }
}
