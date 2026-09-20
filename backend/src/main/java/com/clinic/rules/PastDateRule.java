package com.clinic.rules;

import com.clinic.config.ClinicProperties;
import com.clinic.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** กฎ 1: ห้ามจองย้อนหลัง และต้องจองล่วงหน้าอย่างน้อย N นาที */
@Component
public class PastDateRule extends AbstractBookingRule {

    private final ClinicProperties properties;

    public PastDateRule(ClinicProperties properties) { this.properties = properties; }

    @Override public int order() { return 10; }

    @Override
    public void check(BookingContext ctx) {
        LocalDateTime start = LocalDateTime.of(ctx.date(), ctx.slot().getStart());
        int lead = properties.getBooking().getMinLeadMinutes();
        if (start.isBefore(LocalDateTime.now().plusMinutes(lead))) {
            throw new BusinessRuleException("PAST_DATE",
                    "ต้องจองล่วงหน้าอย่างน้อย " + lead + " นาที ก่อนเวลาตรวจ");
        }
    }
}
