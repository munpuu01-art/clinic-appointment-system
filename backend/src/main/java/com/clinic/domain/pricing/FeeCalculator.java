package com.clinic.domain.pricing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Context ของ Strategy: เลือกวิธีคิดเงินตามประเภทนัด
 * ถ้าไม่มี Strategy เฉพาะ ใช้สูตรพื้นฐาน (ค่าตรวจแพทย์ x ตัวคูณประเภท)
 */
@Component
public class FeeCalculator {

    private final Map<AppointmentType, PricingStrategy> strategies = new EnumMap<>(AppointmentType.class);

    public FeeCalculator(List<PricingStrategy> strategyList) {
        strategyList.forEach(s -> strategies.put(s.appliesTo(), s));
    }

    public BigDecimal calculate(Appointment appointment) {
        PricingStrategy strategy = strategies.get(appointment.getType());
        if (strategy != null) return strategy.calculateFee(appointment);
        return appointment.getDoctor().effectiveFee()
                .multiply(BigDecimal.valueOf(appointment.getType().getFeeFactor()))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
