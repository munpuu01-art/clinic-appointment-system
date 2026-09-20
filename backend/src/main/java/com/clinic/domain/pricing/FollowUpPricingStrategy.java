package com.clinic.domain.pricing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** ติดตามอาการ: คิดครึ่งราคา และผู้สูงอายุลดเพิ่ม 10% */
@Component
public class FollowUpPricingStrategy implements PricingStrategy {

    @Override public AppointmentType appliesTo() { return AppointmentType.FOLLOW_UP; }

    @Override
    public BigDecimal calculateFee(Appointment a) {
        BigDecimal fee = a.getDoctor().effectiveFee()
                .multiply(BigDecimal.valueOf(a.getType().getFeeFactor()));
        if (a.getPatient().isElderly()) {
            fee = fee.multiply(BigDecimal.valueOf(0.9));
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }
}
