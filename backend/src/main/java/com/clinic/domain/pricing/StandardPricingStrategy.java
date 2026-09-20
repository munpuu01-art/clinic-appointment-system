package com.clinic.domain.pricing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ผู้ป่วยใหม่: คิดค่าตรวจเต็ม + ค่าเปิดแฟ้มเวชระเบียน 50 บาท */
@Component
public class StandardPricingStrategy implements PricingStrategy {

    private static final BigDecimal REGISTRATION_FEE = BigDecimal.valueOf(50);

    @Override public AppointmentType appliesTo() { return AppointmentType.NEW_CASE; }

    @Override
    public BigDecimal calculateFee(Appointment a) {
        BigDecimal base = a.getDoctor().effectiveFee();
        BigDecimal fee = base.multiply(BigDecimal.valueOf(a.getType().getFeeFactor()));
        if (a.getPatient().isNewPatient()) fee = fee.add(REGISTRATION_FEE);
        return fee.setScale(2, RoundingMode.HALF_UP);
    }
}
