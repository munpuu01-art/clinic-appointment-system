package com.clinic.notification;

import com.clinic.domain.appointment.Appointment;
import com.clinic.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

/**
 * Template Method Pattern:
 * ลำดับการส่ง (ตรวจสิทธิ์ -> หาเลขผู้รับ -> ประกอบข้อความ -> ส่งจริง -> log)
 * ถูกกำหนดไว้ตายตัวใน send() ส่วนรายละเอียดให้ subclass เติม
 */
public abstract class AbstractNotificationSender implements NotificationSender {

    protected static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Logger log = LoggerFactory.getLogger(AbstractNotificationSender.class);

    @Override
    public final void send(AppointmentEvent event) {
        if (!isEnabled()) return;

        String recipient = resolveRecipient(event.appointment());
        if (recipient == null || recipient.isBlank()) {
            log.debug("ข้ามการส่ง {} เพราะผู้ป่วยไม่มีข้อมูลผู้รับ", channel());
            return;
        }

        String message = composeMessage(event);
        dispatch(recipient, message);
        log.info("[{}] ส่งถึง {} : {}", channel(), recipient, message);
    }

    /** ข้อความมาตรฐานที่ทุกช่องทางใช้ร่วมกัน — subclass override ได้ */
    protected String composeMessage(AppointmentEvent event) {
        Appointment a = event.appointment();
        return String.format("%s | นัดหมายเลขที่ %s วันที่ %s เวลา %s กับ %s",
                event.type().getLabel(),
                a.getAppointmentNo(),
                a.getAppointmentDate().format(DATE_FMT),
                a.getStartTime().format(TIME_FMT),
                a.getDoctor().getDisplayName());
    }

    protected abstract String resolveRecipient(Appointment appointment);

    protected abstract void dispatch(String recipient, String message);
}
