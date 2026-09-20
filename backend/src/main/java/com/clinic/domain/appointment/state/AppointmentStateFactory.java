package com.clinic.domain.appointment.state;

import com.clinic.domain.appointment.AppointmentStatus;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory + Flyweight: แปลง enum สถานะ เป็น object State ที่ใช้ซ้ำได้
 * (State ทุกตัวไม่มี field ที่เปลี่ยนแปลง จึง share instance ได้)
 */
public final class AppointmentStateFactory {

    private static final Map<AppointmentStatus, AppointmentState> REGISTRY =
            new EnumMap<>(AppointmentStatus.class);

    static {
        REGISTRY.put(AppointmentStatus.REQUESTED, new RequestedState());
        REGISTRY.put(AppointmentStatus.CONFIRMED, new ConfirmedState());
        REGISTRY.put(AppointmentStatus.CHECKED_IN, new CheckedInState());
        REGISTRY.put(AppointmentStatus.IN_PROGRESS, new InProgressState());
        REGISTRY.put(AppointmentStatus.COMPLETED, new TerminalState(AppointmentStatus.COMPLETED));
        REGISTRY.put(AppointmentStatus.CANCELLED, new TerminalState(AppointmentStatus.CANCELLED));
        REGISTRY.put(AppointmentStatus.NO_SHOW, new TerminalState(AppointmentStatus.NO_SHOW));
    }

    private AppointmentStateFactory() { }

    public static AppointmentState of(AppointmentStatus status) {
        AppointmentState state = REGISTRY.get(status);
        if (state == null) throw new IllegalArgumentException("ไม่รู้จักสถานะ " + status);
        return state;
    }
}
