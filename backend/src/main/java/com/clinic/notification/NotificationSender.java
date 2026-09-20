package com.clinic.notification;

import com.clinic.event.AppointmentEvent;

/** ช่องทางส่งข้อความถึงผู้ป่วย */
public interface NotificationSender {
    String channel();
    boolean isEnabled();
    void send(AppointmentEvent event);
}
