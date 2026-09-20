package com.clinic.notification;

import com.clinic.config.ClinicProperties;
import com.clinic.domain.appointment.Appointment;
import com.clinic.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** ส่งอีเมล (ตัวอย่างนี้จำลองด้วย log — ต่อ SMTP/SendGrid จริงได้ทันที) */
@Component
public class EmailNotificationSender extends AbstractNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);
    private final ClinicProperties properties;

    public EmailNotificationSender(ClinicProperties properties) { this.properties = properties; }

    @Override public String channel() { return "EMAIL"; }

    @Override public boolean isEnabled() { return properties.getNotification().isEmailEnabled(); }

    @Override
    protected String resolveRecipient(Appointment appointment) {
        return appointment.getPatient().getContact() == null
                ? null : appointment.getPatient().getContact().getEmail();
    }

    @Override
    protected String composeMessage(AppointmentEvent event) {
        Appointment a = event.appointment();
        return "เรียนคุณ " + a.getPatient().getFullName() + "\n"
                + super.composeMessage(event) + "\n"
                + "กรุณามาถึงคลินิกก่อนเวลานัด 15 นาที เพื่อเช็คอินและรับบัตรคิว";
    }

    @Override
    protected void dispatch(String recipient, String message) {
        log.debug("SMTP -> {}", recipient);
    }
}
