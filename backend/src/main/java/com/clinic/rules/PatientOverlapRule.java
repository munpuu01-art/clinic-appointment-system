package com.clinic.rules;

import com.clinic.domain.appointment.Appointment;
import com.clinic.exception.BusinessRuleException;
import com.clinic.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** กฎ 6: ผู้ป่วยคนเดียวกันต้องไม่มีนัดซ้อนเวลากัน (แม้คนละแพทย์) */
@Component
public class PatientOverlapRule extends AbstractBookingRule {

    private final AppointmentRepository appointmentRepository;

    public PatientOverlapRule(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override public int order() { return 60; }

    @Override
    public void check(BookingContext ctx) {
        List<Appointment> sameDay = appointmentRepository
                .findActiveByPatientAndDate(ctx.patient().getId(), ctx.date());

        boolean overlapped = sameDay.stream()
                .filter(a -> !Objects.equals(a.getId(), ctx.excludeAppointmentId()))
                .anyMatch(a -> a.timeSlot().overlaps(ctx.slot()));

        if (overlapped) {
            throw new BusinessRuleException("PATIENT_OVERLAP",
                    "ผู้ป่วยมีนัดหมายอื่นซ้อนกับช่วงเวลานี้อยู่แล้ว");
        }
    }
}
