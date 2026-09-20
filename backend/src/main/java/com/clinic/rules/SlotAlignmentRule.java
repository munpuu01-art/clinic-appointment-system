package com.clinic.rules;

import com.clinic.domain.common.TimeSlot;
import com.clinic.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/** กฎ 4: เวลาที่จองต้องตรงกับช่องเวลาที่ระบบสร้างไว้ (ไม่จองคร่อมช่อง) */
@Component
public class SlotAlignmentRule extends AbstractBookingRule {

    @Override public int order() { return 40; }

    @Override
    public void check(BookingContext ctx) {
        boolean aligned = ctx.doctor().getSchedules().stream()
                .filter(s -> s.appliesOn(ctx.date()))
                .flatMap(s -> s.generateSlots().stream())
                .anyMatch(slot -> slot.equals(ctx.slot()));
        if (!aligned) {
            throw new BusinessRuleException("SLOT_NOT_ALIGNED",
                    "เวลา " + ctx.slot() + " ไม่ตรงกับช่องเวลาที่เปิดให้จอง");
        }
    }

    /** เปิดเป็น helper ให้ service ใช้สร้างรายการช่องว่างได้ด้วย */
    public boolean isAligned(BookingContext ctx, TimeSlot slot) {
        return ctx.doctor().getSchedules().stream()
                .filter(s -> s.appliesOn(ctx.date()))
                .flatMap(s -> s.generateSlots().stream())
                .anyMatch(s -> s.equals(slot));
    }
}
