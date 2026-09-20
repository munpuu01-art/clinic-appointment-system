package com.clinic.notification;

import com.clinic.event.AppointmentEvent;
import com.clinic.event.AppointmentEventType;
import com.clinic.event.AppointmentObserver;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Observer ที่แจ้งเตือนผู้ป่วยผ่านทุกช่องทางที่เปิดใช้งาน */
@Component
public class PatientNotificationObserver implements AppointmentObserver {

    private static final Set<AppointmentEventType> INTERESTED = EnumSet.of(
            AppointmentEventType.BOOKED,
            AppointmentEventType.CONFIRMED,
            AppointmentEventType.RESCHEDULED,
            AppointmentEventType.CANCELLED,
            AppointmentEventType.QUEUE_CALLED
    );

    private final List<NotificationSender> senders;

    public PatientNotificationObserver(List<NotificationSender> senders) { this.senders = senders; }

    @Override public String observerName() { return "PATIENT_NOTIFICATION"; }

    @Override public boolean supports(AppointmentEventType type) { return INTERESTED.contains(type); }

    @Override
    public void onEvent(AppointmentEvent event) {
        senders.forEach(sender -> sender.send(event));
    }
}
