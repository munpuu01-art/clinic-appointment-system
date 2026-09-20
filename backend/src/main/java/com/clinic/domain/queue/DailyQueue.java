package com.clinic.domain.queue;

import com.clinic.domain.doctor.Doctor;
import com.clinic.domain.queue.strategy.QueueOrderingStrategy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Domain object (ไม่ persist): มุมมองคิวของแพทย์ 1 ท่านในวันหนึ่ง
 * ลำดับการเรียกคิวถูกกำหนดโดย Strategy ที่ฉีดเข้ามา
 */
public class DailyQueue {

    private final Doctor doctor;
    private final LocalDate date;
    private final List<QueueTicket> tickets;
    private QueueOrderingStrategy strategy;

    public DailyQueue(Doctor doctor, LocalDate date, List<QueueTicket> tickets,
                      QueueOrderingStrategy strategy) {
        this.doctor = doctor;
        this.date = date;
        this.tickets = new ArrayList<>(tickets);
        this.strategy = strategy;
    }

    /** Strategy เปลี่ยนได้ขณะรันไทม์ เช่น สลับเป็นโหมด FIFO ตอนคนล้น */
    public void changeStrategy(QueueOrderingStrategy strategy) { this.strategy = strategy; }

    public List<QueueTicket> waitingOrdered() {
        List<QueueTicket> waiting = tickets.stream().filter(QueueTicket::isWaiting).toList();
        return strategy.order(waiting);
    }

    public Optional<QueueTicket> peekNext() {
        List<QueueTicket> ordered = waitingOrdered();
        return ordered.isEmpty() ? Optional.empty() : Optional.of(ordered.get(0));
    }

    public Optional<QueueTicket> currentlyServing() {
        return tickets.stream()
                .filter(t -> t.getStatus() == QueueStatus.SERVING || t.getStatus() == QueueStatus.CALLED)
                .findFirst();
    }

    public int waitingCount() { return (int) tickets.stream().filter(QueueTicket::isWaiting).count(); }

    public int doneCount() {
        return (int) tickets.stream().filter(t -> t.getStatus() == QueueStatus.DONE).count();
    }

    /** เวลารอโดยประมาณของคิวถัดไป = จำนวนคิวรอ x เวลาตรวจเฉลี่ยของแผนก */
    public int estimatedWaitMinutes() {
        return waitingCount() * doctor.getSpecialty().getDefaultSlotMinutes();
    }

    public int nextSequenceNo() {
        return tickets.stream().mapToInt(QueueTicket::getSequenceNo).max().orElse(0) + 1;
    }

    public Doctor getDoctor() { return doctor; }
    public LocalDate getDate() { return date; }
    public List<QueueTicket> getTickets() { return List.copyOf(tickets); }
    public String strategyName() { return strategy.name(); }
}
