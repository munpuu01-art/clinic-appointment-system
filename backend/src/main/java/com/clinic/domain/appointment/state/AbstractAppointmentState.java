package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.exception.InvalidAppointmentStateException;

/**
 * Template/ฐานกลางของทุกสถานะ: ค่าเริ่มต้นคือ "ทำไม่ได้"
 * สถานะลูกจะ override เฉพาะการกระทำที่อนุญาต (Open-Closed Principle)
 */
public abstract class AbstractAppointmentState implements AppointmentState {

    @Override public void confirm(Appointment a) { deny(a, "ยืนยันนัด"); }
    @Override public void checkIn(Appointment a) { deny(a, "เช็คอิน"); }
    @Override public void start(Appointment a) { deny(a, "เริ่มตรวจ"); }
    @Override public void complete(Appointment a) { deny(a, "ปิดการตรวจ"); }
    @Override public void cancel(Appointment a, String reason) { deny(a, "ยกเลิกนัด"); }
    @Override public void noShow(Appointment a) { deny(a, "บันทึกไม่มาตามนัด"); }

    protected void deny(Appointment a, String action) {
        throw new InvalidAppointmentStateException(a.getStatus(), action);
    }
}
