package com.clinic.rules;

import com.clinic.domain.appointment.Appointment;
import com.clinic.exception.DoubleBookingException;
import com.clinic.repository.AppointmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** กฎ 5: ช่องเวลาของแพทย์ต้องไม่ถูกจองเกินความจุของช่องนั้น */
@Component
public class DoctorDoubleBookingRule extends AbstractBookingRule {

    private final AppointmentRepository appointmentRepository;

    public DoctorDoubleBookingRule(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override public int order() { return 50; }

    @Override
    public void check(BookingContext ctx) {
        List<Appointment> sameDay = appointmentRepository
                .findActiveByDoctorAndDate(ctx.doctor().getId(), ctx.date());

        long overlapping = sameDay.stream()
                .filter(a -> !Objects.equals(a.getId(), ctx.excludeAppointmentId()))
                .filter(a -> a.timeSlot().overlaps(ctx.slot()))
                .count();

        int capacity = ctx.doctor().getSchedules().stream()
                .filter(s -> s.appliesOn(ctx.date()) && s.covers(ctx.slot()))
                .mapToInt(s -> s.getCapacityPerSlot())
                .max().orElse(1);

        if (overlapping >= capacity) {
            throw new DoubleBookingException(
                    "ช่วงเวลา " + ctx.slot() + " ของแพทย์ถูกจองเต็มแล้ว (ความจุ " + capacity + " คิว)");
        }
    }
}
