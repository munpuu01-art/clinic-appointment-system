package com.clinic.rules;

import com.clinic.config.ClinicProperties;
import com.clinic.domain.appointment.AppointmentType;
import com.clinic.exception.BusinessRuleException;
import com.clinic.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

/** กฎ 7: จำกัดจำนวนนัดต่อผู้ป่วยต่อวัน (ยกเว้นเคสเร่งด่วน) */
@Component
public class PatientDailyLimitRule extends AbstractBookingRule {

    private final AppointmentRepository appointmentRepository;
    private final ClinicProperties properties;

    public PatientDailyLimitRule(AppointmentRepository appointmentRepository, ClinicProperties properties) {
        this.appointmentRepository = appointmentRepository;
        this.properties = properties;
    }

    @Override public int order() { return 70; }

    @Override
    public void check(BookingContext ctx) {
        if (ctx.type() == AppointmentType.URGENT) return;

        int limit = properties.getBooking().getMaxPerPatientPerDay();
        long count = appointmentRepository
                .findActiveByPatientAndDate(ctx.patient().getId(), ctx.date()).size();

        if (count >= limit) {
            throw new BusinessRuleException("DAILY_LIMIT",
                    "ผู้ป่วยจองได้ไม่เกิน " + limit + " นัดต่อวัน");
        }
    }
}
