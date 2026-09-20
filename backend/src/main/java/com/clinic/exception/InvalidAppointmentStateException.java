package com.clinic.exception;

import com.clinic.domain.appointment.AppointmentStatus;

/** เปลี่ยนสถานะนัดหมายผิดลำดับ เช่น COMPLETED แล้วจะ cancel ไม่ได้ */
public class InvalidAppointmentStateException extends BusinessRuleException {
    public InvalidAppointmentStateException(AppointmentStatus current, String action) {
        super("INVALID_STATE",
              "ไม่สามารถ" + action + "ได้ เพราะนัดหมายอยู่ในสถานะ " + current.getLabel());
    }
}
