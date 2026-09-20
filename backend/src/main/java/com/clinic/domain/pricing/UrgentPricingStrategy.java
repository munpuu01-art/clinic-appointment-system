package com.clinic.domain.pricing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;

/** เร่งด่วน: บวกเพิ่มตามตัวคูณ และนอกเวลาราชการ (หลัง 17:00) บวกอีก 300 บาท */
@Component
public class UrgentPricingStrategy implements PricingStrategy {

    private static final BigDecimal AFTER_HOURS_SURCHARGE = BigDecimal.valueOf(300);

    @Override public AppointmentType appliesTo() { return AppointmentType.URGENT; }

    @Override
    public BigDecimal calculateFee(Appointment a) {
        BigDecimal fee = a.getDoctor().effectiveFee()
                .multiply(BigDecimal.valueOf(a.getType().getFeeFactor()));
        if (a.getStartTime().isAfter(LocalTime.of(17, 0))) {
            fee = fee.add(AFTER_HOURS_SURCHARGE);
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }
}
