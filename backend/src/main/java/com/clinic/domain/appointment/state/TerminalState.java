package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.AppointmentStatus;

/** สถานะสุดท้าย (COMPLETED / CANCELLED / NO_SHOW) — เปลี่ยนต่อไม่ได้แล้ว */
public class TerminalState extends AbstractAppointmentState {

    private final AppointmentStatus status;

    public TerminalState(AppointmentStatus status) { this.status = status; }

    @Override public AppointmentStatus status() { return status; }
}
