package com.clinic.domain.pricing;

import com.clinic.domain.appointment.Appointment;
import com.clinic.domain.appointment.AppointmentType;

import java.math.BigDecimal;

/** Strategy Pattern: วิธีคิดค่าบริการตามประเภทการเข้ารับบริการ */
public interface PricingStrategy {
    AppointmentType appliesTo();
    BigDecimal calculateFee(Appointment appointment);
}
