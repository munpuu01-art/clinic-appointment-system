package com.clinic.rules;

import com.clinic.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/** กฎ 3: แพทย์ต้องยังปฏิบัติงาน และช่วงเวลาต้องอยู่ในตารางออกตรวจ + ไม่ติดวันลา */
@Component
public class DoctorAvailabilityRule extends AbstractBookingRule {

    @Override public int order() { return 30; }

    @Override
    public void check(BookingContext ctx) {
        if (!ctx.doctor().isActive()) {
            throw new BusinessRuleException("DOCTOR_INACTIVE",
                    "แพทย์ท่านนี้ไม่ได้ออกตรวจแล้ว");
        }
        if (!ctx.doctor().isAvailableAt(ctx.date(), ctx.slot())) {
            throw new BusinessRuleException("OUT_OF_SCHEDULE",
                    "แพทย์ไม่มีตารางออกตรวจในช่วง " + ctx.slot() + " ของวันที่ " + ctx.date()
                            + " (อาจติดวันลาหรืออยู่นอกเวลาทำการ)");
        }
    }
}
