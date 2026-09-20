package com.clinic.domain.queue.strategy;

import com.clinic.domain.queue.QueueTicket;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

/**
 * เรียงตามเวลานัดหมายจริง — คิว walk-in (ไม่มีนัด) ไปต่อท้าย
 * ยกเว้นเคสฉุกเฉินที่ยังถูกดึงขึ้นมาก่อนเสมอ
 */
@Component
public class AppointmentTimeQueueStrategy implements QueueOrderingStrategy {

    @Override public String name() { return "APPOINTMENT_TIME"; }

    @Override
    public List<QueueTicket> order(List<QueueTicket> waitingTickets) {
        return waitingTickets.stream()
                .sorted(Comparator.comparingInt((QueueTicket t) ->
                                t.getPriority().getWeight() == 0 ? 0 : 1)
                        .thenComparing(this::appointmentTimeOf)
                        .thenComparing(QueueTicket::getIssuedAt))
                .toList();
    }

    private LocalTime appointmentTimeOf(QueueTicket ticket) {
        return ticket.getAppointment() != null
                ? ticket.getAppointment().getStartTime()
                : LocalTime.MAX;
    }
}
