package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentStatus;
import com.clinic.exception.BusinessRuleException;

/** ยืนยันแล้ว: เช็คอินได้เฉพาะวันนัด, ยกเลิกได้, หรือถูกบันทึกว่าไม่มา */
public class ConfirmedState extends AbstractAppointmentState {

    @Override public AppointmentStatus status() { return AppointmentStatus.CONFIRMED; }

    @Override
    public void checkIn(Appointment a) {
        if (!a.isToday()) {
            throw new BusinessRuleException("CHECKIN_NOT_TODAY", "เช็คอินได้เฉพาะวันที่นัดหมายเท่านั้น");
        }
        a.applyStatus(AppointmentStatus.CHECKED_IN);
    }

    @Override
    public void cancel(Appointment a, String reason) {
        a.applyCancelReason(reason);
        a.applyStatus(AppointmentStatus.CANCELLED);
    }

    @Override
    public void noShow(Appointment a) {
        if (!a.isPast()) {
            throw new BusinessRuleException("NOSHOW_TOO_EARLY", "ยังไม่ถึงเวลานัด จึงยังบันทึกไม่มาตามนัดไม่ได้");
        }
        a.applyStatus(AppointmentStatus.NO_SHOW);
    }

    @Override
    public boolean allows(AppointmentStatus target) {
        return target == AppointmentStatus.CHECKED_IN
                || target == AppointmentStatus.CANCELLED
                || target == AppointmentStatus.NO_SHOW;
    }
}
