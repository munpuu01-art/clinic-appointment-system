package com.clinic.notification;

import com.clinic.config.ClinicProperties;
import com.clinic.domain.appointment.Appointment;
import com.clinic.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** ส่ง SMS — ข้อความสั้นกว่าอีเมล (override composeMessage) */
@Component
public class SmsNotificationSender extends AbstractNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);
    private final ClinicProperties properties;

    public SmsNotificationSender(ClinicProperties properties) { this.properties = properties; }

    @Override public String channel() { return "SMS"; }

    @Override public boolean isEnabled() { return properties.getNotification().isSmsEnabled(); }

    @Override
    protected String resolveRecipient(Appointment appointment) {
        return appointment.getPatient().getContact() == null
                ? null : appointment.getPatient().getContact().getPhone();
    }

    @Override
    protected String composeMessage(AppointmentEvent event) {
        Appointment a = event.appointment();
        return String.format("[คลินิก] %s %s %s น. %s",
                event.type().getLabel(),
                a.getAppointmentDate().format(DATE_FMT),
                a.getStartTime().format(TIME_FMT),
                a.getDoctor().getFullName());
    }

    @Override
    protected void dispatch(String recipient, String message) {
        log.debug("SMS gateway -> {}", recipient);
    }
}
