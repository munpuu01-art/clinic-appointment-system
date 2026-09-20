package com.clinic.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Observer ที่บันทึกทุกเหตุการณ์ลง audit log (ต่อยอดเป็นตาราง audit ได้) */
@Component
public class AuditLogObserver implements AppointmentObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditLogObserver.class);

    @Override public String observerName() { return "AUDIT_LOG"; }

    @Override public boolean supports(AppointmentEventType type) { return true; }

    @Override
    public void onEvent(AppointmentEvent event) {
        log.info("AUDIT | {} | appointment={} | patient={} | doctor={} | detail={}",
                event.type(),
                event.appointment().getAppointmentNo(),
                event.appointment().getPatient().getHn(),
                event.appointment().getDoctor().getLicenseNo(),
                event.detail());
    }
}
