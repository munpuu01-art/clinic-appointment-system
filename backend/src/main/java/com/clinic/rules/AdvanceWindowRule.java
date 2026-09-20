package com.clinic.rules;

import com.clinic.config.ClinicProperties;
import com.clinic.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** กฎ 2: จองล่วงหน้าได้ไม่เกินกรอบเวลาที่คลินิกกำหนด */
@Component
public class AdvanceWindowRule extends AbstractBookingRule {

    private final ClinicProperties properties;

    public AdvanceWindowRule(ClinicProperties properties) { this.properties = properties; }

    @Override public int order() { return 20; }

    @Override
    public void check(BookingContext ctx) {
        int days = properties.getBooking().getAdvanceDays();
        if (ctx.date().isAfter(LocalDate.now().plusDays(days))) {
            throw new BusinessRuleException("ADVANCE_WINDOW",
                    "จองล่วงหน้าได้ไม่เกิน " + days + " วัน");
        }
    }
}
