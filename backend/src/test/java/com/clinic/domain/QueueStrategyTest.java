package com.clinic.domain;

import com.clinic.domain.common.ContactInfo;
import com.clinic.domain.common.Gender;
import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.doctor.Specialty;
import com.clinic.domain.person.Patient;
import com.clinic.domain.queue.QueuePriority;
import com.clinic.domain.queue.QueueTicket;
import com.clinic.domain.queue.strategy.FifoQueueStrategy;
import com.clinic.domain.queue.strategy.PriorityQueueStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** TC-09..TC-10 : ทดสอบ Strategy การจัดลำดับคิว */
class QueueStrategyTest {

    private final Specialty specialty = new Specialty("INT", "อายุรกรรม", 20, BigDecimal.valueOf(500));
    private final Doctor doctor = new Doctor("ว.1", specialty, "หมอ", "ทดสอบ", Gender.MALE,
            LocalDate.of(1980, 1, 1), "1", new ContactInfo("0812345678", null, null));

    private QueueTicket ticket(String no, QueuePriority priority, int seq) {
        Patient p = new Patient("HN-" + no, "คนไข้", no, Gender.MALE,
                LocalDate.of(1990, 1, 1), null, new ContactInfo("0812345678", null, null));
        return new QueueTicket(no, LocalDate.now(), doctor, p, priority, seq);
    }

    @Test
    @DisplayName("TC-09 FIFO เรียงตามลำดับการออกบัตร")
    void fifoOrder() {
        List<QueueTicket> tickets = List.of(
                ticket("A001", QueuePriority.NORMAL, 1),
                ticket("A002", QueuePriority.EMERGENCY, 2),
                ticket("A003", QueuePriority.ELDERLY, 3));

        List<QueueTicket> ordered = new FifoQueueStrategy().order(tickets);
        assertEquals("A001", ordered.get(0).getTicketNo());
    }

    @Test
    @DisplayName("TC-10 Priority ดึงเคสฉุกเฉินขึ้นก่อนเสมอ")
    void priorityOrder() {
        List<QueueTicket> tickets = List.of(
                ticket("A001", QueuePriority.NORMAL, 1),
                ticket("A002", QueuePriority.EMERGENCY, 2),
                ticket("A003", QueuePriority.ELDERLY, 3));

        List<QueueTicket> ordered = new PriorityQueueStrategy().order(tickets);
        assertEquals("A002", ordered.get(0).getTicketNo());
        assertEquals("A003", ordered.get(1).getTicketNo());
        assertEquals("A001", ordered.get(2).getTicketNo());
    }
}
